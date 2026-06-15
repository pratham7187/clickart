package com.clickkart.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for POST /api/orders/place
 *
 * Captures the delivery details at checkout time.
 * These are stored verbatim as a snapshot in the orders table —
 * changing your address later does not affect an already-placed order.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderRequest {

    @NotBlank(message = "Delivery address is required")
    @Size(min = 10, max = 500, message = "Address must be between 10 and 500 characters")
    private String address;

    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Pincode must be a valid 6-digit Indian PIN code")
    private String pincode;
}
