package com.tbank.marketplace.service;

import com.tbank.marketplace.exceptions.product_exceptions.ProductNotFoundException;
import com.tbank.marketplace.mapper.ProductMapper;
import com.tbank.marketplace.model.ProductCreate;
import com.tbank.marketplace.model.ProductListResponse;
import com.tbank.marketplace.model.ProductResponse;
import com.tbank.marketplace.model.ProductUpdate;
import com.tbank.marketplace.model.entity.Product;
import com.tbank.marketplace.model.entity.User;
import com.tbank.marketplace.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional
    public ProductResponse createProduct(ProductCreate request, User seller) {

        Product product = productMapper.toEntity(request, seller);
        Product saved = productRepository.save(product);

        log.info("Создан новый товар {} (id={}) пользователем {}", saved.getName(), saved.getId(), seller.getId());
        return productMapper.toResponse(saved);
    }

    public ProductResponse getProduct(UUID id) {
        log.debug("Попытка получить товар: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Товар {} не найден" + id));

        return productMapper.toResponse(product);
    }

    public ProductListResponse getProducts(Integer page, Integer size, String status, String category) {
        log.debug("Getting products: page={}, size={}, status={}, category={}", page, size, status, category);


        Pageable pageable = PageRequest.of(page, size);

        Page<Product> productPage;

        if (status != null && category != null) {
            productPage = productRepository.findByCategoryAndStatus(category, Product.ProductStatus.valueOf(status), pageable);
        } else if (status != null) {
            productPage = productRepository.findByStatus(Product.ProductStatus.valueOf(status), pageable);
        } else if (category != null) {
            productPage = productRepository.findByCategory(category, pageable);
        } else {
            productPage = productRepository.findAll(pageable);
        }

        ProductListResponse response = new ProductListResponse();
        response.setContent(productPage.getContent().stream()
                .map(productMapper::toResponse)
                .toList());
        response.setTotalElements((int) productPage.getTotalElements());
        response.setPageNumber(productPage.getNumber());
        response.setPageSize(productPage.getSize());

        return response;
    }

    @Transactional
    public ProductResponse updateProduct(UUID id, ProductUpdate request, User currentUser) {
        log.info("Пользователь {} пытается обновить продукт {}", currentUser.getId(), id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Товар {} не найден", id);
                    return new ProductNotFoundException("Товар не найден" + id);
                });

        if (!currentUser.getRole().name().equals("ADMIN") && !product.getSellerId().equals(currentUser.getId())) {
            throw new SecurityException("Пользователь не имеет доступ к изменению товара: " + id);
        }

        productMapper.updateEntity(product, request);
        Product updated = productRepository.save(product);

        log.info("Продукт {} (id={}) обновлен пользователем: {} ", updated.getName(), updated.getId(), currentUser.getId());
        return productMapper.toResponse(updated);
    }

    @Transactional
    public void deleteProduct(UUID id, User currentUser) {
        log.info("Попытка удаления товара: {} пользователем: {}", id, currentUser.getId());

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Товар {} не найден" + id));

        if (!currentUser.getRole().name().equals("ADMIN") && !product.getSellerId().equals(currentUser.getId())) {
            throw new SecurityException("Пользователь не имеет доступ к удалению товара: " + id);
        }

        product.softDelete();
        productRepository.save(product);

        log.info("Товар: {} (id={}) удален пользователем {}", product.getName(), product.getId(), currentUser.getId());

    }
}