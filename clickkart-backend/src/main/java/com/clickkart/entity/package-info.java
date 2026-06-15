/**
 * ENTITY LAYER — com.clickkart.entity
 *
 * JPA entity classes. Each class maps directly to one MySQL table.
 * Entities are annotated with @Entity and @Table(name = "table_name").
 *
 * IMPORTANT RULES for entities in this project:
 *   1. NEVER return entities directly from controllers — always map to DTOs first.
 *   2. NEVER add business logic to entity classes — keep them as pure data holders.
 *   3. Use FetchType.LAZY on all @OneToMany and @ManyToOne relationships unless
 *      you have a specific reason to use EAGER (EAGER causes N+1 query problems).
 *   4. Use @Transactional on service methods that load lazy relationships to
 *      prevent LazyInitializationException.
 *
 * Entities in this package (one per DB table):
 *
 *   User.java           → maps to: users
 *   Category.java       → maps to: categories
 *   Product.java        → maps to: products
 *   Cart.java           → maps to: cart
 *   CartItem.java       → maps to: cart_items
 *   WishlistItem.java   → maps to: wishlist_items
 *   Order.java          → maps to: orders
 *   OrderItem.java      → maps to: order_items
 *
 * Relationship annotations that will be used:
 *   @OneToOne          — User ↔ Cart
 *   @OneToMany         — Cart → CartItems, Order → OrderItems, User → Orders
 *   @ManyToOne         — CartItem → Cart, CartItem → Product, etc.
 *   @JoinColumn        — specifies the FK column name in the child table
 */
package com.clickkart.entity;
