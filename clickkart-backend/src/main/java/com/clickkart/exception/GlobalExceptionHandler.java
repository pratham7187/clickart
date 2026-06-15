package com.clickkart.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for all ClickKart REST controllers.
 *
 * Every custom exception maps to a specific HTTP status code and a
 * consistent JSON error body. The response shape is:
 * <pre>
 * {
 *   "success"   : false,
 *   "timestamp" : "2025-01-01T12:00:00",
 *   "status"    : 404,
 *   "error"     : "Not Found",
 *   "message"   : "Product not found with id: '99'",
 *   "path"      : "/api/products/99"
 * }
 * </pre>
 *
 * Field validation errors include an additional "errors" map:
 * <pre>
 * {
 *   "errors": { "email": "Email must be a valid address", "name": "Name is required" }
 * }
 * </pre>
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // =========================================================================
    // 404 — Not Found
    // =========================================================================

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(
            ResourceNotFoundException ex, WebRequest request) {
        log.warn("ResourceNotFoundException: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
    }

    // =========================================================================
    // 409 — Conflict
    // =========================================================================

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateResource(
            DuplicateResourceException ex, WebRequest request) {
        log.warn("DuplicateResourceException: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
    }

    @ExceptionHandler(StockUnavailableException.class)
    public ResponseEntity<Map<String, Object>> handleStockUnavailable(
            StockUnavailableException ex, WebRequest request) {
        log.warn("StockUnavailableException: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
    }

    // =========================================================================
    // 400 — Bad Request
    // =========================================================================

    /**
     * Handles @Valid/@Validated failures on @RequestBody DTOs.
     * Returns a map of field name → validation message for each violated constraint.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        log.warn("Validation failed: {}", fieldErrors);

        Map<String, Object> body = buildBaseErrorBody(
                HttpStatus.BAD_REQUEST,
                "Validation failed. Please check the request fields.",
                request.getDescription(false).replace("uri=", "")
        );
        body.put("errors", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(OrderCancellationException.class)
    public ResponseEntity<Map<String, Object>> handleOrderCancellation(
            OrderCancellationException ex, WebRequest request) {
        log.warn("OrderCancellationException: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex, WebRequest request) {
        log.warn("IllegalArgumentException: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(
            IllegalStateException ex, WebRequest request) {
        log.warn("IllegalStateException: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParam(
            MissingServletRequestParameterException ex, WebRequest request) {
        log.warn("Missing request parameter: {}", ex.getParameterName());
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Required parameter '" + ex.getParameterName() + "' is missing.",
                request.getDescription(false).replace("uri=", "")
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        String msg = String.format("Parameter '%s' must be of type %s.",
                ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        log.warn("MethodArgumentTypeMismatchException: {}", msg);
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                msg,
                request.getDescription(false).replace("uri=", "")
        );
    }

    // =========================================================================
    // 401 — Unauthorized (Spring Security authentication failures)
    // =========================================================================

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(
            BadCredentialsException ex, WebRequest request) {
        log.warn("Authentication failed: bad credentials");
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "Invalid email or password.",
                request.getDescription(false).replace("uri=", "")
        );
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Map<String, Object>> handleDisabled(
            DisabledException ex, WebRequest request) {
        log.warn("Authentication failed: account disabled");
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "Your account has been deactivated. Please contact support.",
                request.getDescription(false).replace("uri=", "")
        );
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<Map<String, Object>> handleLocked(
            LockedException ex, WebRequest request) {
        log.warn("Authentication failed: account locked");
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "Your account is locked. Please contact support.",
                request.getDescription(false).replace("uri=", "")
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthException(
            AuthenticationException ex, WebRequest request) {
        log.warn("AuthenticationException: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "Authentication failed: " + ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
    }

    // =========================================================================
    // 500 — Internal Server Error (catch-all)
    // =========================================================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex, WebRequest request) {
        log.error("Unhandled exception at path {}: {}",
                request.getDescription(false), ex.getMessage(), ex);
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.",
                request.getDescription(false).replace("uri=", "")
        );
    }

    // =========================================================================
    // Builder helpers
    // =========================================================================

    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status, String message, String path) {
        return ResponseEntity
                .status(status)
                .body(buildBaseErrorBody(status, message, path));
    }

    private Map<String, Object> buildBaseErrorBody(
            HttpStatus status, String message, String path) {
        Map<String, Object> body = new HashMap<>();
        body.put("success",   false);
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status",    status.value());
        body.put("error",     status.getReasonPhrase());
        body.put("message",   message);
        body.put("path",      path);
        return body;
    }
}
