/**
 * REPOSITORY LAYER — com.clickkart.repository
 *
 * Spring Data JPA repository interfaces. Each interface extends JpaRepository
 * which automatically provides CRUD operations without implementation code.
 *
 * Custom query methods are declared using:
 *   1. Derived method names (Spring parses these automatically):
 *      findByEmail(String email) → SELECT * FROM users WHERE email = ?
 *
 *   2. @Query with JPQL (object-oriented SQL using entity/field names):
 *      @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.isActive = true")
 *
 *   3. @Query with nativeQuery = true (raw MySQL):
 *      @Query(value = "SELECT * FROM products WHERE MATCH(name, subcategory) AGAINST (?1 IN BOOLEAN MODE)", nativeQuery = true)
 *
 * Repositories in this package:
 *
 *   UserRepository.java          extends JpaRepository<User, Long>
 *     findByEmail(String email)  → Optional<User>
 *     existsByEmail(String email)→ boolean
 *
 *   CategoryRepository.java      extends JpaRepository<Category, Integer>
 *     findByIsActiveTrue()       → List<Category>
 *
 *   ProductRepository.java       extends JpaRepository<Product, Long>
 *     findByCategoryIdAndIsActiveTrue(Integer categoryId, Pageable pageable)
 *     findByCategoryIdAndSubcategoryAndIsActiveTrue(Integer categoryId, String subcategory, Pageable pageable)
 *     searchFullText(String query)  → @Query native FULLTEXT search
 *
 *   CartRepository.java          extends JpaRepository<Cart, Long>
 *     findByUserId(Long userId)  → Optional<Cart>
 *
 *   CartItemRepository.java      extends JpaRepository<CartItem, Long>
 *     findByCartId(Long cartId)  → List<CartItem>
 *     findByCartIdAndProductId(Long cartId, Long productId) → Optional<CartItem>
 *
 *   WishlistItemRepository.java  extends JpaRepository<WishlistItem, Long>
 *     findByUserId(Long userId)  → List<WishlistItem>
 *     findByUserIdAndProductId(Long userId, Long productId) → Optional<WishlistItem>
 *     existsByUserIdAndProductId(Long userId, Long productId) → boolean
 *
 *   OrderRepository.java         extends JpaRepository<Order, Long>
 *     findByUserIdOrderByOrderedAtDesc(Long userId) → List<Order>
 *     findByUserIdAndId(Long userId, Long orderId)  → Optional<Order>
 *     findByStatus(OrderStatus status, Pageable pageable) → Page<Order>
 *
 *   OrderItemRepository.java     extends JpaRepository<OrderItem, Long>
 *     findByOrderId(Long orderId) → List<OrderItem>
 */
package com.clickkart.repository;
