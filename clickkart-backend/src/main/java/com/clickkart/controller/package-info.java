/**
 * CONTROLLER LAYER — com.clickkart.controller
 *
 * REST controllers. Each class maps to a URL namespace and delegates
 * all logic to the corresponding @Service class. Controllers must
 * contain ZERO business logic — only request validation and delegation.
 *
 * Controllers in this package:
 *
 *   AuthController.java      → POST /api/auth/register
 *                              POST /api/auth/login
 *
 *   CategoryController.java  → GET  /api/categories
 *
 *   ProductController.java   → GET  /api/products
 *                              GET  /api/products/{id}
 *
 *   CartController.java      → GET    /api/cart
 *                              POST   /api/cart/items
 *                              PUT    /api/cart/items/{itemId}
 *                              DELETE /api/cart/items/{itemId}
 *                              DELETE /api/cart
 *
 *   WishlistController.java  → GET    /api/wishlist
 *                              POST   /api/wishlist
 *                              DELETE /api/wishlist/{id}
 *                              POST   /api/wishlist/{id}/move-to-cart
 *
 *   OrderController.java     → GET  /api/orders
 *                              GET  /api/orders/{id}
 *                              POST /api/orders/checkout
 *
 *   UserController.java      → GET  /api/user/profile
 *
 *   SearchController.java    → GET  /api/search?query=...
 *
 * Sub-packages:
 *   admin/ → AdminProductController.java  (POST/PUT/DELETE /api/admin/products)
 *            AdminOrderController.java    (GET/PATCH       /api/admin/orders)
 */
package com.clickkart.controller;
