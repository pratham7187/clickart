/**
 * EXCEPTION LAYER — com.clickkart.exception
 *
 * Custom exception classes and the global exception handler.
 * All exceptions are caught in GlobalExceptionHandler and
 * converted to a structured JSON error response.
 *
 * Error response format (for every exception):
 * {
 *   "success": false,
 *   "message": "Human-readable error message",
 *   "timestamp": "2025-06-05T00:45:00",
 *   "path": "/api/cart/items"
 * }
 *
 * Exceptions in this package:
 *
 *   GlobalExceptionHandler.java  (@RestControllerAdvice)
 *     Catches all exceptions application-wide and maps them to HTTP responses.
 *
 *   ResourceNotFoundException.java  (extends RuntimeException)
 *     Thrown when: product not found, order not found, wishlist item not found
 *     HTTP Status: 404 Not Found
 *
 *   EmailAlreadyExistsException.java  (extends RuntimeException)
 *     Thrown when: register attempt with an email that already exists in users table
 *     HTTP Status: 409 Conflict
 *
 *   InvalidCredentialsException.java  (extends RuntimeException)
 *     Thrown when: login email not found or BCrypt hash mismatch
 *     HTTP Status: 401 Unauthorized
 *
 *   OutOfStockException.java  (extends RuntimeException)
 *     Thrown when: checkout requested quantity > available stock
 *     HTTP Status: 400 Bad Request
 *
 *   UnauthorizedAccessException.java  (extends RuntimeException)
 *     Thrown when: user tries to access another user's order/cart
 *     HTTP Status: 403 Forbidden
 *
 *   CartEmptyException.java  (extends RuntimeException)
 *     Thrown when: checkout is attempted with an empty cart
 *     HTTP Status: 400 Bad Request
 *
 *   MethodArgumentNotValidException  (Spring's — already handled by GlobalExceptionHandler)
 *     Thrown when: @Valid fails on a @RequestBody DTO
 *     HTTP Status: 400 Bad Request
 *     Response includes field-level error details
 */
package com.clickkart.exception;
