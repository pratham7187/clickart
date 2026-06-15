package com.clickkart.exception;

/**
 * Thrown during checkout when a product's stock is insufficient
 * to fulfil the requested quantity.
 *
 * The atomic decrementStock() query returns 0 rows updated when stock
 * is less than the requested quantity — that triggers this exception.
 *
 * Mapped to HTTP 409 (Conflict) by the global exception handler.
 */
public class StockUnavailableException extends RuntimeException {

    public StockUnavailableException(String message) {
        super(message);
    }

    public StockUnavailableException(String productName, int requested) {
        super(String.format(
                "Insufficient stock for '%s': requested %d unit(s) but stock is unavailable.",
                productName, requested
        ));
    }
}
