package com.tbank.marketplace.exceptions.order_exceptions;

public class PromoCodeInvalidException extends RuntimeException {
    public PromoCodeInvalidException(String message) {
        super(message);
    }
}
