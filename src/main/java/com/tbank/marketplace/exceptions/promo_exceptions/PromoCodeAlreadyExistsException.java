package com.tbank.marketplace.exceptions.promo_exceptions;

public class PromoCodeAlreadyExistsException extends RuntimeException {
    public PromoCodeAlreadyExistsException(String message) {
        super(message);
    }
}
