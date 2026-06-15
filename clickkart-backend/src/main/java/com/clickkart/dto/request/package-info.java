/**
 * REQUEST DTO LAYER — com.clickkart.dto.request
 *
 * Data Transfer Objects for inbound HTTP request bodies.
 * These classes are annotated with Jakarta Bean Validation constraints
 * and are bound with @RequestBody + @Valid in controllers.
 *
 * DTOs in this package:
 *
 *   LoginRequest.java
 *     String email     @NotBlank @Email
 *     String password  @NotBlank @Size(min = 6)
 *
 *   RegisterRequest.java
 *     String name      @NotBlank @Size(min = 2, max = 100)
 *     String email     @NotBlank @Email
 *     String password  @NotBlank @Size(min = 8, max = 50)
 *
 *   CartItemRequest.java
 *     Long productId   @NotNull @Positive
 *     Integer quantity @NotNull @Min(1) @Max(99)
 *
 *   UpdateCartItemRequest.java
 *     Integer quantity @NotNull @Min(1) @Max(99)
 *
 *   CheckoutRequest.java
 *     String pincode   @NotBlank @Pattern(regexp = "\\d{6}")
 *     String address   @NotBlank @Size(min = 10, max = 500)
 *
 *   WishlistRequest.java
 *     Long productId   @NotNull @Positive
 *
 *   ProductRequest.java      (Admin only)
 *     String name            @NotBlank @Size(max = 200)
 *     BigDecimal price       @NotNull @DecimalMin("0.01")
 *     String imageUrl        @NotBlank
 *     Integer categoryId     @NotNull @Positive
 *     String subcategory     @NotBlank
 *     String description
 *     Integer stock          @NotNull @Min(0)
 *
 *   OrderStatusRequest.java  (Admin only)
 *     String status    @NotBlank (validated against OrderStatus enum in service)
 */
package com.clickkart.dto.request;
