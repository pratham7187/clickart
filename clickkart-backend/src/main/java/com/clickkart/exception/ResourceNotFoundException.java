package com.clickkart.exception;

/**
 * Thrown when a requested entity does not exist in the database.
 * Mapped to HTTP 404 by the global exception handler.
 *
 * Examples:
 *   - Product not found by ID
 *   - User not found by email
 *   - Order not found for a given user
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
    }
}
