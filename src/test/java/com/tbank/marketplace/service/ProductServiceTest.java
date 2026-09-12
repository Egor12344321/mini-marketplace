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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    private final UUID productId = UUID.randomUUID();
    private final UUID sellerId = UUID.randomUUID();

    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    @Test
    void createProduct_savesAndReturnsResponse() {
        User seller = seller();
        Product product = product(Product.ProductStatus.ACTIVE, sellerId);
        when(productMapper.toEntity(any(ProductCreate.class), eq(seller))).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);
        ProductResponse expected = new ProductResponse();
        when(productMapper.toResponse(product)).thenReturn(expected);

        ProductResponse result = productService.createProduct(productCreate(), seller);

        assertThat(result).isSameAs(expected);
        verify(productRepository).save(product);
    }

    @Test
    void getProduct_found_returnsResponse() {
        Product product = product(Product.ProductStatus.ACTIVE, sellerId);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        ProductResponse expected = new ProductResponse();
        when(productMapper.toResponse(product)).thenReturn(expected);

        assertThat(productService.getProduct(productId)).isSameAs(expected);
    }

    @Test
    void getProduct_notFound_throwsProductNotFound() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProduct(productId))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void getProducts_withoutFilters_usesFindAll() {
        Product product = product(Product.ProductStatus.ACTIVE, sellerId);
        Page<Product> page = new PageImpl<>(List.of(product), PageRequest.of(0, 10), 1);
        when(productRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(productMapper.toResponse(any(Product.class))).thenReturn(new ProductResponse());

        ProductListResponse response = productService.getProducts(0, 10, null, null);

        assertThat(response.getContent()).hasSize(1);
        verify(productRepository).findAll(any(Pageable.class));
    }

    @Test
    void updateProduct_owner_saves() {
        Product product = product(Product.ProductStatus.ACTIVE, sellerId);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(new ProductResponse());

        productService.updateProduct(productId, new ProductUpdate(), seller());

        verify(productRepository).save(product);
    }

    @Test
    void updateProduct_notOwner_throwsSecurityException() {
        Product product = product(Product.ProductStatus.ACTIVE, sellerId);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        User otherSeller = seller();
        otherSeller.setId(UUID.randomUUID());

        assertThatThrownBy(() -> productService.updateProduct(productId, new ProductUpdate(), otherSeller))
                .isInstanceOf(SecurityException.class);

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProduct_adminCanUpdate() {
        UUID anotherSellerId = UUID.randomUUID();
        Product product = product(Product.ProductStatus.ACTIVE, anotherSellerId);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(new ProductResponse());

        productService.updateProduct(productId, new ProductUpdate(), admin());

        verify(productRepository).save(product);
    }

    @Test
    void deleteProduct_softDeletes() {
        Product product = product(Product.ProductStatus.ACTIVE, sellerId);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        productService.deleteProduct(productId, seller());

        assertThat(product.getStatus()).isEqualTo(Product.ProductStatus.ARCHIVED);
        verify(productRepository).save(product);
    }

    @Test
    void deleteProduct_notOwner_throwsSecurityException() {
        Product product = product(Product.ProductStatus.ACTIVE, sellerId);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        User otherSeller = seller();
        otherSeller.setId(UUID.randomUUID());

        assertThatThrownBy(() -> productService.deleteProduct(productId, otherSeller))
                .isInstanceOf(SecurityException.class);

        verify(productRepository, never()).save(any(Product.class));
    }

    private Product product(Product.ProductStatus status, UUID sellerId) {
        Product product = new Product();
        product.setId(productId);
        product.setName("Товар");
        product.setDescription("Описание");
        product.setPrice(new BigDecimal("10.00"));
        product.setStock(5);
        product.setCategory("Категория");
        product.setStatus(status);
        product.setSellerId(sellerId);
        return product;
    }

    private User seller() {
        return User.builder()
                .id(sellerId)
                .email("seller@example.com")
                .role(User.UserRole.SELLER)
                .build();
    }

    private User admin() {
        return User.builder()
                .id(UUID.randomUUID())
                .email("admin@example.com")
                .role(User.UserRole.ADMIN)
                .build();
    }

    private ProductCreate productCreate() {
        ProductCreate request = new ProductCreate();
        request.setName("Товар");
        request.setDescription("Описание");
        request.setPrice(new BigDecimal("10.00"));
        request.setStock(5);
        request.setCategory("Категория");
        return request;
    }
}