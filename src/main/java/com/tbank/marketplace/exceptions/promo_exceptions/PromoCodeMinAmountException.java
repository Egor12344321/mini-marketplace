package com.tbank.marketplace.exceptions.promo_exceptions;

public class PromoCodeMinAmountException extends RuntimeException {
    public PromoCodeMinAmountException(String message) {
        super(message);
    }
}
