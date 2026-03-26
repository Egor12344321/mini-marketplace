package com.tbank.marketplace.controller;

import com.tbank.marketplace.api.ProductsApi;
import com.tbank.marketplace.model.ProductCreate;
import com.tbank.marketplace.model.ProductListResponse;
import com.tbank.marketplace.model.ProductResponse;
import com.tbank.marketplace.model.ProductUpdate;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public class ProductController implements ProductsApi {

    @Override
    public ResponseEntity<ProductResponse> createProduct(ProductCreate productCreate) {
        return null;
    }

    @Override
    public ResponseEntity<Void> deleteProduct(UUID id) {
        return null;
    }

    @Override
    public ResponseEntity<ProductResponse> getProduct(UUID id) {
        return null;
    }

    @Override
    public ResponseEntity<ProductListResponse> getProducts(Integer page, Integer size, @Nullable String status, @Nullable String category) {
        return null;
    }

    @Override
    public ResponseEntity<ProductResponse> updateProduct(UUID id, ProductUpdate productUpdate) {
        return null;
    }
}
