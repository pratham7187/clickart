package com.clickkart.dto.response;

import com.clickkart.entity.CartItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private Long id;
    private ProductResponse product;
    private Integer quantity;
    private BigDecimal subtotal;
    private LocalDateTime addedAt;

    public static CartItemResponse from(CartItem item) {
        BigDecimal price = item.getProduct().getPrice();
        BigDecimal subtotal = price.multiply(BigDecimal.valueOf(item.getQuantity()));
        return CartItemResponse.builder()
                .id(item.getId())
                .product(ProductResponse.from(item.getProduct()))
                .quantity(item.getQuantity())
                .subtotal(subtotal)
                .addedAt(item.getAddedAt())
                .build();
    }
}
