package com.tbank.marketplace.repository;

import com.tbank.marketplace.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Page<Product> findProductsByStatus(Product.ProductStatus status, Pageable pageable);
    Page<Product> findByCategoryAndStatus(String category, Product.ProductStatus status, Pageable pageable);
    Optional<Product> findByIdAndSellerId(UUID id, UUID sellerId);

}
