package com.tbank.marketplace.exceptions.promo_exceptions;

public class PromoCodeInvalidException extends RuntimeException {
    public PromoCodeInvalidException(String message) {
        super(message);
    }
}
