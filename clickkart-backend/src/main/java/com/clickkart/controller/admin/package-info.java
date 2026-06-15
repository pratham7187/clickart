/**
 * ADMIN CONTROLLER LAYER — com.clickkart.controller.admin
 *
 * Controllers for ADMIN-only endpoints.
 * All endpoints in this sub-package are secured with ROLE_ADMIN in SecurityConfig.
 *
 *   AdminProductController.java
 *     POST   /api/admin/products          → create a new product
 *     PUT    /api/admin/products/{id}     → update product details / price / stock
 *     DELETE /api/admin/products/{id}     → soft-delete (is_active = false)
 *     POST   /api/admin/categories        → create a new category
 *
 *   AdminOrderController.java
 *     GET    /api/admin/orders            → paginated list of all orders (filterable by status)
 *     PATCH  /api/admin/orders/{id}/status → update order status lifecycle
 */
package com.clickkart.controller.admin;
