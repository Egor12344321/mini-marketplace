package com.tbank.marketplace.service;

import com.tbank.marketplace.exceptions.order_exceptions.InsufficientStockException;
import com.tbank.marketplace.exceptions.product_exceptions.ProductNotFoundException;
import com.tbank.marketplace.mapper.OrderMapper;
import com.tbank.marketplace.model.OrderCreateRequest;
import com.tbank.marketplace.model.OrderItemRequest;
import com.tbank.marketplace.model.OrderUpdateRequest;
import com.tbank.marketplace.model.entity.Order;
import com.tbank.marketplace.model.entity.OrderItem;
import com.tbank.marketplace.model.entity.Product;
import com.tbank.marketplace.model.entity.PromoCode;
import com.tbank.marketplace.model.entity.UserOperation;
import com.tbank.marketplace.repository.OrderItemRepository;
import com.tbank.marketplace.repository.OrderRepository;
import com.tbank.marketplace.repository.ProductRepository;
import com.tbank.marketplace.repository.PromoCodeRepository;
import com.tbank.marketplace.repository.UserOperationsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private PromoCodeRepository promoCodeRepository;
    @Mock
    private UserOperationsRepository userOperationsRepository;
    @Mock
    private RateLimiterService rateLimiterService;

    @InjectMocks
    private OrderService orderService;

    private final UUID userId = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();
    private final UUID promoId = UUID.randomUUID();

    @Test
    void createOrder_reservesStock_andIncrementsPromoUsageOnce() {
        Product product = product(10, "100.00");
        PromoCode promo = promo(0, "0");
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(product));
        when(promoCodeRepository.findByCode("SAVE10")).thenReturn(Optional.of(promo));
        when(rateLimiterService.canCreateOrder(userId)).thenReturn(true);
        when(orderRepository.hasActiveOrders(userId)).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderCreateRequest request = createRequest("SAVE10", 2);

        orderService.createOrder(request, userId);

        assertThat(product.getStock()).isEqualTo(8);
        assertThat(promo.getCurrentUses()).isEqualTo(1);
        verify(userOperationsRepository).save(argThat(op ->
                op.getOperationType() == UserOperation.OperationType.CREATE_ORDER));
    }

    @Test
    void updateOrder_keepingPromo_doesNotDoubleCountUsage() {
        Product product = product(10, "100.00");
        PromoCode promo = promo(1, "0");
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(product));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order(Order.OrderStatus.CREATED, promoId)));
        when(orderItemRepository.findByOrderId(orderId)).thenReturn(List.of(orderItem(product, 2)));
        when(promoCodeRepository.findById(promoId)).thenReturn(Optional.of(promo));
        when(rateLimiterService.canUpdateOrder(userId)).thenReturn(true);

        orderService.updateOrder(userId, orderId, updateRequest(2));

        assertThat(promo.getCurrentUses()).isEqualTo(1);
        verify(promoCodeRepository, never()).save(any(PromoCode.class));
    }

    @Test
    void updateOrder_removingPromo_decrementsUsage() {
        Product product = product(10, "100.00");
        PromoCode promo = promo(1, "1000");
        Order order = order(Order.OrderStatus.CREATED, promoId);
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(product));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(orderId)).thenReturn(List.of(orderItem(product, 2)));
        when(promoCodeRepository.findById(promoId)).thenReturn(Optional.of(promo));
        when(rateLimiterService.canUpdateOrder(userId)).thenReturn(true);

        orderService.updateOrder(userId, orderId, updateRequest(2));

        assertThat(promo.getCurrentUses()).isEqualTo(0);
        assertThat(order.getPromoCodeId()).isNull();
        verify(promoCodeRepository).save(promo);
    }

    @Test
    void cancelOrder_restoresStock_andDecrementsPromoUsage() {
        Product product = product(5, "100.00");
        PromoCode promo = promo(1, "0");
        Order order = order(Order.OrderStatus.CREATED, promoId);
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(product));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(orderId)).thenReturn(List.of(orderItem(product, 2)));
        when(promoCodeRepository.findById(promoId)).thenReturn(Optional.of(promo));

        orderService.cancelOrder(userId, orderId);

        assertThat(product.getStock()).isEqualTo(7);
        assertThat(promo.getCurrentUses()).isEqualTo(0);
        assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.CANCELED);
    }

    @Test
    void createOrder_productNotFound_throwsProductNotFoundException() {
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.empty());
        when(rateLimiterService.canCreateOrder(userId)).thenReturn(true);
        when(orderRepository.hasActiveOrders(userId)).thenReturn(false);

        assertThatThrownBy(() -> orderService.createOrder(createRequest(null, 1), userId))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void createOrder_insufficientStock_throwsInsufficientStockException() {
        Product product = product(1, "100.00");
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(product));
        when(rateLimiterService.canCreateOrder(userId)).thenReturn(true);
        when(orderRepository.hasActiveOrders(userId)).thenReturn(false);

        assertThatThrownBy(() -> orderService.createOrder(createRequest(null, 5), userId))
                .isInstanceOf(InsufficientStockException.class);
    }

    private Product product(int stock, String price) {
        Product product = new Product();
        product.setId(productId);
        product.setName("Товар");
        product.setPrice(new BigDecimal(price));
        product.setStock(stock);
        product.setCategory("Категория");
        product.setStatus(Product.ProductStatus.ACTIVE);
        product.setSellerId(UUID.randomUUID());
        return product;
    }

    private PromoCode promo(int currentUses, String minAmount) {
        LocalDateTime now = LocalDateTime.now();
        return PromoCode.builder()
                .id(promoId)
                .code("SAVE10")
                .discountType(PromoCode.DiscountType.FIXED_AMOUNT)
                .discountValue(new BigDecimal("10"))
                .minOrderAmount(new BigDecimal(minAmount))
                .maxUses(100)
                .currentUses(currentUses)
                .validFrom(now.minusDays(1))
                .validUntil(now.plusDays(1))
                .active(true)
                .build();
    }

    private Order order(Order.OrderStatus status, UUID promoCodeId) {
        return Order.builder()
                .id(orderId)
                .userId(userId)
                .status(status)
                .promoCodeId(promoCodeId)
                .totalAmount(new BigDecimal("0"))
                .discountAmount(BigDecimal.ZERO)
                .build();
    }

    private OrderItem orderItem(Product product, int quantity) {
        return OrderItem.builder()
                .id(UUID.randomUUID())
                .order(Order.builder().id(orderId).userId(userId).build())
                .product(product)
                .quantity(quantity)
                .priceAtOrder(product.getPrice())
                .build();
    }

    private OrderCreateRequest createRequest(String promoCode, int quantity) {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setItems(List.of(item(quantity)));
        request.setPromoCode(promoCode);
        return request;
    }

    private OrderUpdateRequest updateRequest(int quantity) {
        return new OrderUpdateRequest(List.of(item(quantity)));
    }

    private OrderItemRequest item(int quantity) {
        return new OrderItemRequest(productId, quantity);
    }
}