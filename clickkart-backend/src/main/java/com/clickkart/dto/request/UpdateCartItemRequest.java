package com.clickkart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for PUT /api/cart/update
 *
 * Sends the absolute desired quantity for a product line in the cart.
 * Sending quantity = 0 will trigger item removal.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCartItemRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be 0 or greater (0 removes the item)")
    private Integer quantity;
}
