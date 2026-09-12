package com.tbank.marketplace.exceptions;

import com.tbank.marketplace.exceptions.order_exceptions.OrderNotFoundException;
import com.tbank.marketplace.exceptions.product_exceptions.ProductNotFoundException;
import com.tbank.marketplace.model.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void productNotFound_mapsTo404WithCode() {
        ResponseEntity<ErrorResponse> response =
                handler.handleProductNotFoundException(new ProductNotFoundException("Товар не найден"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getErrorCode()).isEqualTo("PRODUCT_NOT_FOUND");
        assertThat(response.getBody().getMessage()).isEqualTo("Товар не найден");
    }

    @Test
    void orderNotFound_mapsTo404WithCode() {
        ResponseEntity<ErrorResponse> response =
                handler.handleOrderNotFoundException(new OrderNotFoundException("Заказ не найден"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getErrorCode()).isEqualTo("ORDER_NOT_FOUND");
    }

    @Test
    void illegalArgument_mapsTo400InsteadOf500() {
        ResponseEntity<ErrorResponse> response =
                handler.handleIllegalArgumentException(new IllegalArgumentException("невалидное значение"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getErrorCode()).isEqualTo("BAD_REQUEST");
    }
}