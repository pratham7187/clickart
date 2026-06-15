package com.clickkart.dto.response;

import com.clickkart.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private BigDecimal totalAmount;
    private String address;
    private String pincode;
    private String status;
    private LocalDateTime orderedAt;
    private LocalDateTime updatedAt;
    private List<OrderItemResponse> items;

    /**
     * Full order response including line items.
     * Use for order detail / receipt page.
     * Assumes order.getItems() are already loaded (JOIN FETCH in repository).
     */
    public static OrderResponse from(Order order) {
        List<OrderItemResponse> items = (order.getItems() != null)
                ? order.getItems().stream().map(OrderItemResponse::from).toList()
                : Collections.emptyList();
        return OrderResponse.builder()
                .id(order.getId())
                .totalAmount(order.getTotalAmount())
                .address(order.getAddress())
                .pincode(order.getPincode())
                .status(order.getStatus().name())
                .orderedAt(order.getOrderedAt())
                .updatedAt(order.getUpdatedAt())
                .items(items)
                .build();
    }

    /**
     * Summary response (no items) for the order history list page.
     * Avoids loading items for every order in the list.
     */
    public static OrderResponse summary(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .totalAmount(order.getTotalAmount())
                .address(order.getAddress())
                .pincode(order.getPincode())
                .status(order.getStatus().name())
                .orderedAt(order.getOrderedAt())
                .updatedAt(order.getUpdatedAt())
                .items(Collections.emptyList())
                .build();
    }
}
