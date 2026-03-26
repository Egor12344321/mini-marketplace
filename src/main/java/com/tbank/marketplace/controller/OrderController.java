package com.tbank.marketplace.controller;

import com.tbank.marketplace.api.OrdersApi;
import com.tbank.marketplace.model.OrderCreateRequest;
import com.tbank.marketplace.model.OrderResponse;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public class OrderController implements OrdersApi {

    @Override
    public ResponseEntity<Void> cancelOrder(UUID id) {
        return null;
    }

    @Override
    public ResponseEntity<OrderResponse> createOrder(OrderCreateRequest orderCreateRequest) {
        return null;
    }

    @Override
    public ResponseEntity<OrderResponse> getOrder(UUID id) {
        return null;
    }

    @Override
    public ResponseEntity<OrderResponse> updateOrder(UUID id, OrderCreateRequest orderCreateRequest) {
        return null;
    }
}
