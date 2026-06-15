package com.clickkart.exception;

/**
 * Thrown when an attempt is made to create a resource that already exists
 * and the business rule requires uniqueness.
 *
 * Examples:
 *   - Adding a product to a wishlist that already contains it
 *   - Registering with an email that is already in use
 *
 * Mapped to HTTP 409 (Conflict) by the global exception handler.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s already exists with %s: '%s'", resourceName, fieldName, fieldValue));
    }
}
