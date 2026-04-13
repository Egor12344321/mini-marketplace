package com.tbank.marketplace.exceptions.order_exceptions;

public class ProductInactiveException extends RuntimeException {
    public ProductInactiveException(String message) {
        super(message);
    }
}
