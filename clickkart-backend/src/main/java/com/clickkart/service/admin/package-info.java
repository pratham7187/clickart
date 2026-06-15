/**
 * ADMIN SERVICE LAYER — com.clickkart.service.admin
 *
 *   AdminProductService.java
 *     createProduct(ProductRequest)         → ProductResponse
 *     updateProduct(Long id, ProductRequest)→ ProductResponse
 *     softDeleteProduct(Long id)            → void  (sets is_active = false)
 *     createCategory(CategoryRequest)       → CategoryResponse
 *
 *   AdminOrderService.java
 *     getAllOrders(OrderStatus status, Pageable) → Page<OrderResponse>
 *     updateOrderStatus(Long orderId, String newStatus) → OrderResponse
 */
package com.clickkart.service.admin;
