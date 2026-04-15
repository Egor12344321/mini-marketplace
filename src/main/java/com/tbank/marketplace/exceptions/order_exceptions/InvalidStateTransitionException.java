package com.tbank.marketplace.exceptions.order_exceptions;

public class InvalidStateTransitionException extends RuntimeException {
    public InvalidStateTransitionException(String message) {
        super(message);
    }
}
