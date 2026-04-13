package com.tbank.marketplace.controller;

import com.tbank.marketplace.api.OrdersApi;
import com.tbank.marketplace.model.OrderCreateRequest;
import com.tbank.marketplace.model.OrderResponse;
import com.tbank.marketplace.model.entity.User;
import com.tbank.marketplace.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;


@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/api/orders")
public class OrderController implements OrdersApi {

    private final OrderService orderService;

    @Override
    public ResponseEntity<Void> cancelOrder(UUID id) {
        return null;
    }

    @Override
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderCreateRequest orderCreateRequest) {
        log.debug("Stared creating new order");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        OrderResponse orderResponse = orderService.createOrder(orderCreateRequest, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
    }

    @Override
    public ResponseEntity<OrderResponse> getOrder(UUID id) {
        OrderResponse orderResponse = orderService.getOrder(id);
        return null;
    }

    @Override
    public ResponseEntity<OrderResponse> updateOrder(UUID id, OrderCreateRequest orderCreateRequest) {
        return null;
    }
}
