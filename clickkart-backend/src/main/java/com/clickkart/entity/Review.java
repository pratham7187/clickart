package com.clickkart.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "reviews",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_reviews_user_product",
        columnNames = {"user_id", "product_id"}
    ),
    indexes = {
        @Index(name = "idx_reviews_product", columnList = "product_id"),
        @Index(name = "idx_reviews_user", columnList = "user_id")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_reviews_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_reviews_product"))
    private Product product;

    @NotNull
    @Min(1) @Max(5)
    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Size(max = 1000)
    @Column(name = "comment", length = 1000)
    private String comment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
