package com.tbank.marketplace.exceptions.order_exceptions;

public class RateLimitingCreateOrderException extends RuntimeException {
  public RateLimitingCreateOrderException(String message) {
    super(message);
  }
}
