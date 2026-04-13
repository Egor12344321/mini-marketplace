package com.tbank.marketplace.service;


import com.tbank.marketplace.exceptions.order_exceptions.*;
import com.tbank.marketplace.mapper.OrderMapper;
import com.tbank.marketplace.model.OrderCreateRequest;
import com.tbank.marketplace.model.OrderItemRequest;
import com.tbank.marketplace.model.OrderResponse;
import com.tbank.marketplace.model.entity.*;
import com.tbank.marketplace.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderMapper orderMapper;
    private final ProductRepository productRepository;
    private final PromoCodeRepository promoCodeRepository;
    private final UserOperationsRepository userOperationsRepository;
    private final RateLimiterService rateLimiterService;

    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request, User user) {
        UUID userId = user.getId();
        log.info("Создание заказа для пользователя: {}", userId);

        rateLimitCheck(userId);
        activeOrderCheck(userId);

        List<Product> products = loadAndValidateProducts(request.getItems());
        reserveStock(products, request.getItems());

        BigDecimal subtotal = calculateSubtotal(products, request.getItems());
        PromoCode promoCode = null;
        BigDecimal discount = BigDecimal.ZERO;

        if (request.getPromoCode() != null && !request.getPromoCode().isEmpty()) {
            promoCode = validatePromoCode(request.getPromoCode(), subtotal);
            discount = calculateDiscount(promoCode, subtotal);
        }

        BigDecimal total = subtotal.subtract(discount);

        Order order = saveOrder(userId, total, discount, promoCode);
        List<OrderItem> orderItems = saveOrderItems(order, products, request.getItems());

        if (promoCode != null) {
            promoCode.setCurrentUses(promoCode.getCurrentUses() + 1);
            promoCodeRepository.save(promoCode);
        }

        saveUserOperation(userId, UserOperation.OperationType.CREATE_ORDER);

        log.info("Заказ {} создан пользователем {}, сумма: {}, скидка: {}, количество позиций: {}", order.getId(), userId, total, discount, request.getItems().size());
        return orderMapper.toResponse(order, orderItems, promoCode);
    }

    private void rateLimitCheck(UUID userId) {
        if (!rateLimiterService.canCreateOrder(userId)) {
            long minutes = rateLimiterService.getRemainingMinutes(userId);
            throw new RateLimitingCreateOrderException(String.format("Следующий заказ через %d минут", minutes));
        }
    }

    private void activeOrderCheck(UUID userId) {
        if (orderRepository.hasActiveOrders(userId)) {
            throw new OrderHasActiveException("У вас уже есть активный заказ");
        }
    }

    private List<Product> loadAndValidateProducts(List<OrderItemRequest> items) {
        List<Product> products = new ArrayList<>();
        List<InsufficientStockException.StockError> stockErrors = new ArrayList<>();

        for (OrderItemRequest item : items) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException("Товар не найден: " + item.getProductId()));

            if (product.getStatus() != Product.ProductStatus.ACTIVE) {
                throw new ProductInactiveException("Товар неактивен: " + product.getId());
            }

            if (product.getStock() < item.getQuantity()) {
                stockErrors.add(new InsufficientStockException.StockError(product.getId().toString(), product.getStock(), item.getQuantity()));
            }

            products.add(product);
        }

        if (!stockErrors.isEmpty()) {
            throw new InsufficientStockException("Недостаточно товара на складе", stockErrors);
        }

        return products;
    }

    private void reserveStock(List<Product> products, List<OrderItemRequest> items) {
        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            int quantity = items.get(i).getQuantity();
            product.setStock(product.getStock() - quantity);
            productRepository.save(product);
        }
    }

    private BigDecimal calculateSubtotal(List<Product> products, List<OrderItemRequest> items) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (int i = 0; i < products.size(); i++) {
            BigDecimal price = products.get(i).getPrice();
            int quantity = items.get(i).getQuantity();
            subtotal = subtotal.add(price.multiply(BigDecimal.valueOf(quantity)));
        }
        return subtotal;
    }

    private PromoCode validatePromoCode(String code, BigDecimal subtotal) {
        PromoCode promo = promoCodeRepository.findByCode(code)
                .orElseThrow(() -> new PromoCodeInvalidException("Промокод не найден"));

        LocalDateTime now = LocalDateTime.now();

        if (!promo.getActive()) {
            log.warn("Пользователь ввел неактивный промокод: {}", code);
            throw new PromoCodeInvalidException("Промокод неактивен");
        }
        if (promo.getCurrentUses() >= promo.getMaxUses()) {
            log.warn("Пользователь ввел промокод, превысивший лимит использования: {}", code);
            throw new PromoCodeInvalidException("Промокод использован максимальное число раз");
        }
        if (now.isBefore(promo.getValidFrom()) || now.isAfter(promo.getValidUntil())) {
            log.warn("Пользователь ввел просроченный промокод: {}", code);
            throw new PromoCodeInvalidException("Срок действия промокода истёк");
        }
        if (subtotal.compareTo(promo.getMinOrderAmount()) < 0) {
            log.info("Сумма заказа {} меньше минимальной {} для промокода {}", subtotal, promo.getMinOrderAmount(), code);
            throw new PromoCodeMinAmountException(String.format("Минимальная сумма заказа: %s", promo.getMinOrderAmount()));
        }

        log.debug("Промокод {} успешно применен к сумме {}", code, subtotal);
        return promo;
    }

    private BigDecimal calculateDiscount(PromoCode promo, BigDecimal subtotal) {
        if (promo.getDiscountType() == PromoCode.DiscountType.PERCENTAGE) {
            BigDecimal discount = subtotal.multiply(promo.getDiscountValue()).divide(BigDecimal.valueOf(100), BigDecimal.ROUND_HALF_UP);
            BigDecimal maxDiscount = subtotal.multiply(BigDecimal.valueOf(0.7));
            return discount.min(maxDiscount);
        } else {
            return promo.getDiscountValue().min(subtotal);
        }
    }

    private Order saveOrder(UUID userId, BigDecimal total, BigDecimal discount, PromoCode promo) {
        Order order = Order.builder()
                .userId(userId)
                .status(Order.OrderStatus.CREATED)
                .totalAmount(total)
                .discountAmount(discount)
                .promoCodeId(promo != null ? promo.getId() : null)
                .build();
        return orderRepository.save(order);
    }

    private List<OrderItem> saveOrderItems(Order order, List<Product> products, List<OrderItemRequest> items) {

        List<OrderItem> orderItems = new ArrayList<>();

        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            int quantity = items.get(i).getQuantity();

            OrderItem item = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(quantity)
                    .priceAtOrder(product.getPrice())
                    .build();
            orderItems.add(orderItemRepository.save(item));
        }

        return orderItems;
    }

    private void saveUserOperation(UUID userId, UserOperation.OperationType type) {
        UserOperation op = new UserOperation(userId, type);
        userOperationsRepository.save(op);
    }

}