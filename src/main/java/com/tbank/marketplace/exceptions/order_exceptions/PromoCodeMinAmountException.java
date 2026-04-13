package com.tbank.marketplace.exceptions.order_exceptions;

public class PromoCodeMinAmountException extends RuntimeException {
    public PromoCodeMinAmountException(String message) {
        super(message);
    }
}
