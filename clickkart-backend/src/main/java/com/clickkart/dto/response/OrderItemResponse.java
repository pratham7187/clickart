package com.clickkart.dto.response;

import com.clickkart.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    private Long id;
    private ProductResponse product;
    private Integer quantity;
    private BigDecimal priceAtPurchase;
    private BigDecimal lineTotal;

    public static OrderItemResponse from(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .product(ProductResponse.from(item.getProduct()))
                .quantity(item.getQuantity())
                .priceAtPurchase(item.getPriceAtPurchase())
                .lineTotal(item.getLineTotal())
                .build();
    }
}
