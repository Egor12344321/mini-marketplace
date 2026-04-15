package com.tbank.marketplace.controller;

import com.tbank.marketplace.api.OrdersApi;
import com.tbank.marketplace.model.OrderCreateRequest;
import com.tbank.marketplace.model.OrderResponse;
import com.tbank.marketplace.model.OrderUpdateRequest;
import com.tbank.marketplace.model.entity.User;
import com.tbank.marketplace.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/api/orders")
public class OrderController implements OrdersApi {

    private final OrderService orderService;

    @Override
    public ResponseEntity<Void> cancelOrder(UUID orderId, @RequestAttribute("userId") UUID userId) {
        log.debug("Stared cancelling order: {}", orderId);
        orderService.cancelOrder(userId, orderId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    public ResponseEntity<OrderResponse> createOrder(@RequestAttribute("userId") UUID userId, @RequestBody OrderCreateRequest orderCreateRequest) {
        log.debug("Stared creating new order");
        OrderResponse orderResponse = orderService.createOrder(orderCreateRequest, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
    }

    @Override
    public ResponseEntity<OrderResponse> getOrder(UUID orderId, @RequestAttribute("userId") UUID userId) {
        OrderResponse orderResponse = orderService.getOrder(orderId, userId);
        return ResponseEntity.ok(orderResponse);
    }

    @Override
    public ResponseEntity<OrderResponse> updateOrder(@PathVariable UUID orderId, @RequestAttribute("userId") UUID userId, @RequestBody OrderUpdateRequest orderUpdateRequest) {
        log.debug("Stared updating order: {}", orderId);
        OrderResponse orderResponse = orderService.updateOrder(userId, orderId, orderUpdateRequest);
        return ResponseEntity.ok(orderResponse);
    }

}
