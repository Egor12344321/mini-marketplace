package com.tbank.marketplace.mapper;

import com.tbank.marketplace.model.OrderItemResponse;
import com.tbank.marketplace.model.OrderResponse;
import com.tbank.marketplace.model.entity.Order;
import com.tbank.marketplace.model.entity.OrderItem;
import com.tbank.marketplace.model.entity.PromoCode;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order, List<OrderItem> items, PromoCode promoCode) {
        OrderResponse response = new OrderResponse();

        response.setId(order.getId());
        response.setUserId(order.getUserId());
        response.setStatus(OrderResponse.StatusEnum.valueOf(order.getStatus().name()));
        response.setTotalAmount(order.getTotalAmount());
        response.setDiscountAmount(order.getDiscountAmount());

        if (promoCode != null) {
            response.setPromoCode(JsonNullable.of(promoCode.getCode()));
        } else {
            response.setPromoCode(JsonNullable.undefined());
        }

        if (order.getCreatedAt() != null) {
            response.setCreatedAt(order.getCreatedAt().atOffset(ZoneOffset.UTC));
        }

        if (order.getUpdatedAt() != null) {
            response.setUpdatedAt(order.getUpdatedAt().atOffset(ZoneOffset.UTC));
        }

        List<OrderItemResponse> itemResponses = items.stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList());
        response.setItems(itemResponses);

        return response;
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        OrderItemResponse response = new OrderItemResponse();

        response.setProductId(item.getProduct().getId());
        response.setProductName(item.getProduct().getName());
        response.setQuantity(item.getQuantity());
        response.setPriceAtOrder(item.getPriceAtOrder());

        return response;
    }
}