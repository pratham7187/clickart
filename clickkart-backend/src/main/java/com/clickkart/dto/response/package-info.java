/**
 * RESPONSE DTO LAYER — com.clickkart.dto.response
 *
 * Data Transfer Objects for outbound HTTP response bodies.
 * These are plain Java records or @Data Lombok classes serialised to JSON by Jackson.
 *
 * RULE: Service methods return these, NEVER raw @Entity classes.
 * This decouples the API contract from the DB schema — you can
 * change a column name without changing the API.
 *
 * DTOs in this package:
 *
 *   ApiResponse.java              Generic success/error envelope
 *     boolean success
 *     String  message
 *     T       data                (generic payload, null on errors)
 *
 *   AuthResponse.java
 *     String  token
 *     String  name
 *     String  email
 *     String  role
 *
 *   CategoryResponse.java
 *     Integer id
 *     String  name
 *     String  displayName
 *     String  imageUrl
 *
 *   ProductResponse.java
 *     Long    id
 *     String  name
 *     BigDecimal price
 *     String  imageUrl
 *     String  category           (from Category.displayName)
 *     String  subcategory
 *     String  description
 *     Integer stock
 *
 *   CartItemResponse.java
 *     Long    id
 *     ProductResponse product
 *     Integer quantity
 *     BigDecimal lineTotal       (quantity × product.price — calculated in mapper)
 *
 *   CartResponse.java
 *     Long    id
 *     List<CartItemResponse> items
 *     BigDecimal totalAmount     (sum of all lineTotals)
 *     int     itemCount
 *
 *   WishlistResponse.java
 *     Long    id
 *     ProductResponse product
 *     LocalDateTime addedAt
 *
 *   OrderItemResponse.java
 *     Long    id
 *     ProductResponse product
 *     Integer quantity
 *     BigDecimal priceAtPurchase
 *     BigDecimal lineTotal       (quantity × priceAtPurchase)
 *
 *   OrderResponse.java
 *     Long    id
 *     List<OrderItemResponse> items
 *     BigDecimal totalAmount
 *     String  pincode
 *     String  address
 *     String  status
 *     LocalDateTime orderedAt
 *
 *   UserProfileResponse.java
 *     String  name
 *     String  email
 *     String  role
 *     LocalDateTime createdAt
 */
package com.clickkart.dto.response;
