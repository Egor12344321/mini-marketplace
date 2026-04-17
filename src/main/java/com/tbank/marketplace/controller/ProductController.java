package com.tbank.marketplace.controller;

import com.tbank.marketplace.api.ProductsApi;
import com.tbank.marketplace.model.ProductCreate;
import com.tbank.marketplace.model.ProductListResponse;
import com.tbank.marketplace.model.ProductResponse;
import com.tbank.marketplace.model.ProductUpdate;
import com.tbank.marketplace.model.entity.User;
import com.tbank.marketplace.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ProductController implements ProductsApi {

    private final ProductService productService;

    @Override
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<ProductResponse> createProduct(ProductCreate productCreate) {
        User currentUser = getCurrentUser();
        ProductResponse response = productService.createProduct(productCreate, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<ProductResponse> getProduct(UUID id) {
        log.info("Получение товара: {}", id);
        ProductResponse response = productService.getProduct(id);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ProductListResponse> getProducts(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category) {

        log.info("Получение всех товаров: page={}, size={}, status={}, category={}", page, size, status, category);

        ProductListResponse response = productService.getProducts(page, size, status, category);
        return ResponseEntity.ok(response);
    }

    @Override
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<ProductResponse> updateProduct(UUID id, ProductUpdate productUpdate) {
        User currentUser = getCurrentUser();
        ProductResponse response = productService.updateProduct(id, productUpdate, currentUser);
        return ResponseEntity.ok(response);
    }

    @Override
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<Void> deleteProduct(UUID id) {
        User currentUser = getCurrentUser();
        productService.deleteProduct(id, currentUser);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}
