package com.clickkart.dto.response;

import com.clickkart.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private Integer id;
    private String name;
    private String displayName;
    private String description;
    private String imageUrl;
    private Integer sortOrder;
    private boolean active;

    public static CategoryResponse from(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .displayName(category.getDisplayName())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .sortOrder(category.getSortOrder())
                .active(category.isActive())
                .build();
    }
}
