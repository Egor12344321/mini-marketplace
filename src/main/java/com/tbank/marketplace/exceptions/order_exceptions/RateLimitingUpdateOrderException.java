package com.tbank.marketplace.exceptions.order_exceptions;

public class RateLimitingUpdateOrderException extends RuntimeException {
    public RateLimitingUpdateOrderException(String message) {
        super(message);
    }
}
