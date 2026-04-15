package com.tbank.marketplace.exceptions.order_exceptions;

public class OrderOwnershipViolationException extends RuntimeException {
  public OrderOwnershipViolationException(String message) {
    super(message);
  }
}
