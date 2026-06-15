package com.clickkart.dto.response;

import com.clickkart.entity.Product;
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
public class ProductResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private String imageUrl;
    private Integer categoryId;
    private String categoryName;
    private String categoryDisplayName;
    private String subcategory;
    private String description;
    private Integer stock;
    private boolean active;
    private LocalDateTime createdAt;

    public static ProductResponse from(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .imageUrl(product.getImageUrl())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .categoryDisplayName(product.getCategory().getDisplayName())
                .subcategory(product.getSubcategory())
                .description(product.getDescription())
                .stock(product.getStock())
                .active(product.isActive())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
