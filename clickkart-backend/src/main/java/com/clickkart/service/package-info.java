/**
 * SERVICE LAYER — com.clickkart.service
 *
 * Business logic lives here. Services are @Transactional where needed
 * (especially OrderService.checkout() which must be atomic).
 *
 * Rules for this layer:
 *   - NEVER access HttpServletRequest or Spring MVC objects here
 *   - NEVER return @Entity objects — always map to DTOs before returning
 *   - Use SecurityContextHolder.getContext().getAuthentication().getName()
 *     to identify the current user (email from JWT), never accept userId
 *     or email as a method parameter from the controller
 *
 * Services in this package:
 *
 *   AuthService.java
 *     register(RegisterRequest) → creates user with BCrypt hash → returns AuthResponse + JWT
 *     login(LoginRequest)       → verifies BCrypt hash → returns AuthResponse + JWT
 *
 *   CategoryService.java
 *     getAllCategories() → List<CategoryResponse>
 *
 *   ProductService.java
 *     getAllProducts(categoryId, subcategory, pageable) → Page<ProductResponse>
 *     getProductById(Long id)                          → ProductResponse
 *     search(String query)                             → List<ProductResponse>
 *
 *   CartService.java
 *     getCart(String email)                            → CartResponse
 *     addItem(String email, CartItemRequest)           → CartResponse
 *     updateItemQuantity(String email, Long itemId, int quantity) → CartResponse
 *     removeItem(String email, Long itemId)            → CartResponse
 *     clearCart(String email)                          → void
 *
 *   WishlistService.java
 *     getWishlist(String email)                        → List<WishlistResponse>
 *     addToWishlist(String email, WishlistRequest)     → WishlistResponse
 *     removeFromWishlist(String email, Long itemId)    → void
 *     moveToCart(String email, Long itemId)            → CartResponse
 *
 *   OrderService.java  @Transactional
 *     checkout(String email, CheckoutRequest)          → OrderResponse
 *     getUserOrders(String email)                      → List<OrderResponse>
 *     getOrderById(String email, Long orderId)         → OrderResponse
 *
 *   UserService.java
 *     getProfile(String email)                         → UserProfileResponse
 *
 * Sub-packages:
 *   admin/ → AdminProductService.java, AdminOrderService.java
 */
package com.clickkart.service;
