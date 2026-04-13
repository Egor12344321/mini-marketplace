package com.tbank.marketplace.exceptions.order_exceptions;

import lombok.Getter;
import java.util.List;

@Getter
public class InsufficientStockException extends RuntimeException {

    private final List<StockError> errors;

    public InsufficientStockException(String message) {
        super(message);
        this.errors = List.of();
    }

    public InsufficientStockException(String message, List<StockError> errors) {
        super(message);
        this.errors = errors;
    }

    @Getter
    public static class StockError {
        private final String productId;
        private final int availableStock;
        private final int requestedQuantity;

        public StockError(String productId, int availableStock, int requestedQuantity) {
            this.productId = productId;
            this.availableStock = availableStock;
            this.requestedQuantity = requestedQuantity;
        }
    }
}