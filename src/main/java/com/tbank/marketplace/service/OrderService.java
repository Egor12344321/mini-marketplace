package com.tbank.marketplace.service;

import com.tbank.marketplace.exceptions.order_exceptions.*;
import com.tbank.marketplace.exceptions.product_exceptions.ProductInactiveException;
import com.tbank.marketplace.exceptions.product_exceptions.ProductNotFoundException;
import com.tbank.marketplace.exceptions.promo_exceptions.PromoCodeInvalidException;
import com.tbank.marketplace.exceptions.promo_exceptions.PromoCodeMinAmountException;
import com.tbank.marketplace.mapper.OrderMapper;
import com.tbank.marketplace.model.OrderCreateRequest;
import com.tbank.marketplace.model.OrderItemRequest;
import com.tbank.marketplace.model.OrderResponse;
import com.tbank.marketplace.model.OrderUpdateRequest;
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
    public OrderResponse createOrder(OrderCreateRequest request, UUID userId) {
        log.info("Создание заказа для пользователя: {}", userId);

        rateLimitCheckForCreate(userId);
        activeOrderCheck(userId);

        List<Product> products = loadAndValidateProducts(request.getItems());
        reserveStock(products, request.getItems());

        BigDecimal subtotal = calculateSubtotal(products, request.getItems());
        PromoCode promoCode = null;
        BigDecimal discount = BigDecimal.ZERO;

        if (hasPromoCode(request.getPromoCode())) {
            promoCode = validatePromoCode(request.getPromoCode(), subtotal);
            discount = calculateDiscount(promoCode, subtotal);
        }

        BigDecimal total = subtotal.subtract(discount);

        Order order = saveOrder(userId, total, discount, promoCode);
        List<OrderItem> orderItems = saveOrderItems(order, products, request.getItems());

        updatePromoCodeUsage(promoCode, true);
        saveUserOperation(userId, UserOperation.OperationType.CREATE_ORDER);

        log.info("Заказ {} создан, сумма: {}, скидка: {}", order.getId(), total, discount);
        return orderMapper.toResponse(order, orderItems, promoCode);
    }

    @Transactional
    public OrderResponse updateOrder(UUID userId, UUID orderId, OrderUpdateRequest request) {
        log.info("Обновление заказа {} для пользователя {}", orderId, userId);

        Order order = getOrderById(orderId);
        validateOrderOwnership(order, userId);

        if (order.getStatus() != Order.OrderStatus.CREATED){
            log.warn("Попытка обновить заказ {} в статусе {}", orderId, order.getStatus());
            throw new InvalidStateTransitionException("Обновление заказа доступно только в статусе CREATED");
        }

        rateLimitCheckForUpdate(userId);

        List<OrderItem> oldItems = orderItemRepository.findByOrderId(orderId);
        restoreStock(oldItems);

        List<Product> newProducts = loadAndValidateProducts(request.getItems());
        reserveStock(newProducts, request.getItems());

        BigDecimal subtotal = calculateSubtotal(newProducts, request.getItems());

        PromoCode promoCode = null;
        BigDecimal discount = BigDecimal.ZERO;

        if (order.getPromoCodeId() != null) {
            promoCode = promoCodeRepository.findById(order.getPromoCodeId()).orElse(null);
            if (promoCode != null && subtotal.compareTo(promoCode.getMinOrderAmount()) >= 0) {
                discount = calculateDiscount(promoCode, subtotal);
                log.debug("Промокод {} остаётся применён, скидка: {}", promoCode.getCode(), discount);
            } else if (promoCode != null) {
                BigDecimal minOrderAmount = promoCode.getMinOrderAmount();
                updatePromoCodeUsage(promoCode, false);
                order.setPromoCodeId(null);
                promoCode = null;
                log.info("Промокод отменён, сумма {} меньше минимальной {}", subtotal, minOrderAmount);
            }
        }

        BigDecimal total = subtotal.subtract(discount);

        updateOrderDetails(order, total, discount, promoCode);

        orderItemRepository.deleteAll(oldItems);
        List<OrderItem> newOrderItems = saveOrderItems(order, newProducts, request.getItems());

        saveUserOperation(userId, UserOperation.OperationType.UPDATE_ORDER);

        log.info("Заказ {} обновлён, сумма: {}, скидка: {}", order.getId(), total, discount);
        return orderMapper.toResponse(order, newOrderItems, promoCode);
    }

    @Transactional
    public void cancelOrder(UUID userId, UUID orderId){

        log.info("Отмена заказа {} для пользователя {}", orderId, userId);

        Order order = getOrderById(orderId);

        validateOrderOwnership(order, userId);

        if (!order.canTransitionTo(Order.OrderStatus.CANCELED)){
            log.warn("Попытка удаления товара в недопустимом состоянии");
            throw new InvalidStateTransitionException("Заказ не может быть отменен");
        }

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);

        restoreStock(items);
        PromoCode promoCode;
        if (order.getPromoCodeId() != null) {
            promoCode = promoCodeRepository.findById(order.getPromoCodeId()).orElse(null);
            updatePromoCodeUsage(promoCode, false);
        }

        order.setStatus(Order.OrderStatus.CANCELED);
        orderRepository.save(order);

        log.info("Заказ {} был отменен владельцем {}", orderId, userId);
    }


    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID orderId, UUID userId) {
        Order order = getOrderById(orderId);
        validateOrderOwnership(order, userId);
        List<OrderItem> items = order.getItems();
        UUID promoCodeId = order.getPromoCodeId();
        PromoCode promoCode = null;
        if (promoCodeId != null) {
            promoCode = promoCodeRepository.findById(promoCodeId)
                    .orElse(null);
        }
        return orderMapper.toResponse(order, items, promoCode);
    }


    private Order getOrderById(UUID orderId){
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Заказ не найден: " + orderId));
    }

    private boolean hasPromoCode(String promoCode) {
        return promoCode != null && !promoCode.isEmpty();
    }

    private void rateLimitCheckForCreate(UUID userId) {
        if (!rateLimiterService.canCreateOrder(userId)) {
            long minutes = rateLimiterService.getRemainingMinutesForCreate(userId);
            throw new RateLimitingCreateOrderException(
                    String.format("Следующий заказ через %d минут", minutes));
        }
    }

    private void rateLimitCheckForUpdate(UUID userId) {
        if (!rateLimiterService.canUpdateOrder(userId)) {
            long minutes = rateLimiterService.getRemainingMinutesForUpdate(userId);
            throw new RateLimitingUpdateOrderException(
                    String.format("Следующее обновление заказа через %d минут", minutes));
        }
    }

    private void activeOrderCheck(UUID userId) {
        if (orderRepository.hasActiveOrders(userId)) {
            throw new OrderHasActiveException("У вас уже есть активный заказ");
        }
    }

    private void validateOrderOwnership(Order order, UUID userId) {
        if (!order.getUserId().equals(userId)) {
            log.warn("Пользователь {} пытается обновить/удалить чужой заказ {}", userId, order.getId());
            throw new OrderOwnershipViolationException("Заказ принадлежит другому пользователю");
        }
    }

    private List<Product> loadAndValidateProducts(List<OrderItemRequest> items) {
        List<Product> products = new ArrayList<>();
        List<InsufficientStockException.StockError> stockErrors = new ArrayList<>();

        for (OrderItemRequest item : items) {
            Product product = productRepository.findByIdForUpdate(item.getProductId())
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

    private void restoreStock(List<OrderItem> items) {
        for (OrderItem item : items) {
            Product product = productRepository.findByIdForUpdate(item.getProduct().getId())
                    .orElseThrow(() -> new ProductNotFoundException("Товар не найден: " + item.getProduct().getId()));
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
            log.debug("Возвращено {} шт товара {}", item.getQuantity(), product.getId());
        }
    }

    private BigDecimal calculateSubtotal(List<Product> products, List<OrderItemRequest> items) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (int i = 0; i < products.size(); i++) {
            subtotal = subtotal.add(products.get(i).getPrice()
                    .multiply(BigDecimal.valueOf(items.get(i).getQuantity())));
        }
        return subtotal;
    }

    private PromoCode validatePromoCode(String code, BigDecimal subtotal) {
        PromoCode promo = promoCodeRepository.findByCode(code)
                .orElseThrow(() -> new PromoCodeInvalidException("Промокод не найден"));

        LocalDateTime now = LocalDateTime.now();

        if (!promo.getActive()) {
            log.warn("Неактивный промокод: {}", code);
            throw new PromoCodeInvalidException("Промокод неактивен");
        }
        if (promo.getCurrentUses() >= promo.getMaxUses()) {
            log.warn("Превышен лимит использования промокода: {}", code);
            throw new PromoCodeInvalidException("Промокод использован максимальное число раз");
        }
        if (now.isBefore(promo.getValidFrom()) || now.isAfter(promo.getValidUntil())) {
            log.warn("Просроченный промокод: {}", code);
            throw new PromoCodeInvalidException("Срок действия промокода истёк");
        }
        if (subtotal.compareTo(promo.getMinOrderAmount()) < 0) {
            log.warn("Сумма {} меньше минимальной {} для промокода {}", subtotal, promo.getMinOrderAmount(), code);
            throw new PromoCodeMinAmountException(
                    String.format("Минимальная сумма заказа: %s", promo.getMinOrderAmount()));
        }

        log.debug("Промокод {} применён", code);
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

    private void updateOrderDetails(Order order, BigDecimal total, BigDecimal discount, PromoCode promo) {
        order.setTotalAmount(total);
        order.setDiscountAmount(discount);
        order.setPromoCodeId(promo != null ? promo.getId() : null);
        orderRepository.save(order);
    }

    private List<OrderItem> saveOrderItems(Order order, List<Product> products, List<OrderItemRequest> items) {
        List<OrderItem> orderItems = new ArrayList<>();
        for (int i = 0; i < products.size(); i++) {
            OrderItem item = OrderItem.builder()
                    .order(order)
                    .product(products.get(i))
                    .quantity(items.get(i).getQuantity())
                    .priceAtOrder(products.get(i).getPrice())
                    .build();
            orderItems.add(item);
        }
        return orderItemRepository.saveAll(orderItems);
    }

    private void updatePromoCodeUsage(PromoCode promoCode, boolean increment) {
        if (promoCode == null) return;

        if (increment) {
            promoCode.setCurrentUses(promoCode.getCurrentUses() + 1);
            log.debug("Увеличено использование промокода {} до {}", promoCode.getCode(), promoCode.getCurrentUses());
        } else {
            promoCode.setCurrentUses(promoCode.getCurrentUses() - 1);
            log.debug("Уменьшено использование промокода {} до {}", promoCode.getCode(), promoCode.getCurrentUses());
        }
        promoCodeRepository.save(promoCode);
    }

    private void saveUserOperation(UUID userId, UserOperation.OperationType type) {
        UserOperation op = new UserOperation(userId, type);
        userOperationsRepository.save(op);
    }


}