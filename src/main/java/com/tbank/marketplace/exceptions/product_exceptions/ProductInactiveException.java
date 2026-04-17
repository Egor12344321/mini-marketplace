package com.tbank.marketplace.exceptions.product_exceptions;

public class ProductInactiveException extends RuntimeException {
    public ProductInactiveException(String message) {
        super(message);
    }
}
