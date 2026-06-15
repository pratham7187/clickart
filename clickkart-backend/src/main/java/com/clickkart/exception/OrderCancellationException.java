package com.clickkart.exception;

/**
 * Thrown when an order cannot be cancelled due to its current lifecycle status.
 *
 * Business rule:
 *   Orders can only be cancelled if their status is PLACED or CONFIRMED.
 *   Once an order is SHIPPED or DELIVERED, cancellation is no longer possible.
 *
 * Mapped to HTTP 400 (Bad Request) by the global exception handler.
 */
public class OrderCancellationException extends RuntimeException {

    public OrderCancellationException(String message) {
        super(message);
    }

    public OrderCancellationException(Long orderId, String currentStatus) {
        super(String.format(
                "Order #%d cannot be cancelled. Current status is '%s'. " +
                "Only PLACED or CONFIRMED orders can be cancelled.",
                orderId, currentStatus
        ));
    }
}
