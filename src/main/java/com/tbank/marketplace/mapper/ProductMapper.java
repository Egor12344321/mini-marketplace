package com.tbank.marketplace.mapper;

import com.tbank.marketplace.model.ProductCreate;
import com.tbank.marketplace.model.ProductResponse;
import com.tbank.marketplace.model.ProductUpdate;
import com.tbank.marketplace.model.entity.Product;
import com.tbank.marketplace.model.entity.Product.ProductStatus;
import com.tbank.marketplace.model.entity.User;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;

@Component
public class ProductMapper {

    public Product toEntity(ProductCreate request, User seller) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setCategory(request.getCategory());
        product.setStatus(ProductStatus.ACTIVE);
        product.setSellerId(seller.getId());
        return product;
    }

    public ProductResponse toResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setStock(product.getStock());
        response.setCategory(product.getCategory());
        response.setStatus(ProductResponse.StatusEnum.valueOf(product.getStatus().name()));
        response.setCreatedAt(product.getCreatedAt().atOffset(ZoneOffset.UTC));
        response.setUpdatedAt(product.getUpdatedAt().atOffset(ZoneOffset.UTC));

        return response;
    }

    public void updateEntity(Product product, ProductUpdate request) {
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getStock() != null) {
            product.setStock(request.getStock());
        }
        if (request.getCategory() != null) {
            product.setCategory(request.getCategory());
        }
        if (request.getStatus() != null) {
            product.setStatus(ProductStatus.valueOf(request.getStatus().name()));
        }
    }
}