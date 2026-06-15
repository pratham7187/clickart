package com.clickkart.dto.response;

import com.clickkart.entity.CartItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private Long cartId;
    private List<CartItemResponse> items;
    private int itemCount;
    private BigDecimal totalAmount;

    /**
     * Build CartResponse from an explicit list of CartItem entities.
     * Use this overload when items are already loaded via JOIN FETCH
     * (CartService.getCartItems() returns List<CartItem>).
     */
    public static CartResponse from(Long cartId, List<CartItem> items) {
        if (items == null) items = Collections.emptyList();
        List<CartItemResponse> itemResponses = items.stream()
                .map(CartItemResponse::from)
                .toList();
        BigDecimal total = itemResponses.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return CartResponse.builder()
                .cartId(cartId)
                .items(itemResponses)
                .itemCount(itemResponses.size())
                .totalAmount(total)
                .build();
    }

    public static CartResponse empty(Long cartId) {
        return CartResponse.builder()
                .cartId(cartId)
                .items(Collections.emptyList())
                .itemCount(0)
                .totalAmount(BigDecimal.ZERO)
                .build();
    }
}
