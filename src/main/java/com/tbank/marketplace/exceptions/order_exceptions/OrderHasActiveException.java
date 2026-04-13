package com.tbank.marketplace.exceptions.order_exceptions;

public class OrderHasActiveException extends RuntimeException {
    public OrderHasActiveException(String message) {
        super(message);
    }
}
