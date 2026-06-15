package com.clickkart.service.impl;

import com.clickkart.entity.Product;
import com.clickkart.entity.User;
import com.clickkart.entity.WishlistItem;
import com.clickkart.exception.DuplicateResourceException;
import com.clickkart.exception.ResourceNotFoundException;
import com.clickkart.repository.ProductRepository;
import com.clickkart.repository.UserRepository;
import com.clickkart.repository.WishlistItemRepository;
import com.clickkart.service.CartService;
import com.clickkart.service.WishlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of {@link WishlistService}.
 *
 * The wishlist is a simple M:N bookmark: one user can wishlist many products,
 * one product can be wishlisted by many users.
 * UNIQUE (user_id, product_id) prevents duplicate entries.
 *
 * CartService is injected here (not the reverse) to prevent circular dependencies.
 * WishlistService → CartService: safe.
 * CartService → WishlistService: would be circular.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class WishlistServiceImpl implements WishlistService {

    private final WishlistItemRepository wishlistItemRepository;
    private final ProductRepository      productRepository;
    private final UserRepository         userRepository;
    private final CartService            cartService;

    // =========================================================================
    // Read
    // =========================================================================

    /**
     * {@inheritDoc}
     *
     * Uses JOIN FETCH to load product details in a single query,
     * avoiding N+1 when the wishlist page renders each product card.
     */
    @Override
    public List<WishlistItem> getWishlist(Long userId) {
        log.debug("Fetching wishlist for userId={}", userId);
        return wishlistItemRepository.findByUserIdWithProduct(userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isProductWishlisted(Long userId, Long productId) {
        return wishlistItemRepository.existsByUserIdAndProductId(userId, productId);
    }

    // =========================================================================
    // Add
    // =========================================================================

    /**
     * {@inheritDoc}
     *
     * Guards:
     *   - Product must exist and be active.
     *   - Product must not already be in the user's wishlist.
     *
     * The second guard gives a clean DuplicateResourceException rather than
     * relying on the DB to throw a DataIntegrityViolationException from the
     * UNIQUE constraint — which would produce a 500 error without this check.
     */
    @Override
    @Transactional
    public WishlistItem addToWishlist(Long userId, Long productId) {
        // Validate product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (!product.isActive()) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }

        // Check for existing wishlist entry (idempotency guard)
        if (wishlistItemRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new DuplicateResourceException(
                    String.format("Product '%s' is already in your wishlist.", product.getName())
            );
        }

        // Load user (needed to set the FK relationship)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        WishlistItem item = WishlistItem.builder()
                .user(user)
                .product(product)
                .build();

        WishlistItem saved = wishlistItemRepository.save(item);
        log.info("Product {} added to wishlist for userId={}", productId, userId);
        return saved;
    }

    // =========================================================================
    // Remove
    // =========================================================================

    /**
     * {@inheritDoc}
     *
     * Uses a targeted JPQL DELETE. Idempotent: no-op if the entry doesn't exist.
     * Requires @Transactional because the repository method is @Modifying.
     */
    @Override
    @Transactional
    public void removeFromWishlist(Long userId, Long productId) {
        wishlistItemRepository.deleteByUserIdAndProductId(userId, productId);
        log.info("Product {} removed from wishlist for userId={}", productId, userId);
    }

    // =========================================================================
    // Move to cart
    // =========================================================================

    /**
     * {@inheritDoc}
     *
     * Atomic sequence:
     *   1. Verify the item exists in the wishlist (throws if not).
     *   2. Add the product to the cart via CartService (throws on stock failure).
     *   3. Only if step 2 succeeds, remove the product from the wishlist.
     *
     * If CartService.addToCart() throws (e.g., StockUnavailableException),
     * the @Transactional rollback ensures the wishlist entry is NOT removed.
     */
    @Override
    @Transactional
    public void moveToCart(Long userId, Long productId, Integer quantity) {
        // Verify the wishlist entry exists before attempting cart add
        if (!wishlistItemRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new ResourceNotFoundException(
                    String.format("Product %d is not in the wishlist for user %d", productId, userId)
            );
        }

        // addToCart handles stock validation and throws on failure
        cartService.addToCart(userId, productId, quantity);

        // Remove from wishlist only after successful cart add
        wishlistItemRepository.deleteByUserIdAndProductId(userId, productId);

        log.info("Product {} moved from wishlist to cart for userId={}", productId, userId);
    }
}
