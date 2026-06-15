package com.clickkart.dto.response;

import com.clickkart.entity.WishlistItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistItemResponse {
    private Long id;
    private ProductResponse product;
    private LocalDateTime addedAt;

    public static WishlistItemResponse from(WishlistItem item) {
        return WishlistItemResponse.builder()
                .id(item.getId())
                .product(ProductResponse.from(item.getProduct()))
                .addedAt(item.getAddedAt())
                .build();
    }
}
