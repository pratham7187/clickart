package com.clickkart.service;

import com.clickkart.dto.response.ReviewResponse;
import com.clickkart.entity.*;
import com.clickkart.exception.DuplicateResourceException;
import com.clickkart.exception.ResourceNotFoundException;
import com.clickkart.repository.ProductRepository;
import com.clickkart.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    @Transactional
    public ReviewResponse addReview(User user, Long productId, Integer rating, String comment) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (reviewRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            throw new DuplicateResourceException("Review", "user+product",
                    user.getId() + "+" + productId);
        }

        Review review = Review.builder()
                .user(user)
                .product(product)
                .rating(rating)
                .comment(comment)
                .build();

        review = reviewRepository.save(review);
        return ReviewResponse.from(review);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByProduct(Long productId) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId)
                .stream()
                .map(ReviewResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getReviewSummary(Long productId) {
        Double avg = reviewRepository.findAverageRatingByProductId(productId);
        Long count = reviewRepository.countByProductId(productId);
        Map<String, Object> summary = new HashMap<>();
        summary.put("averageRating", Math.round(avg * 10.0) / 10.0);
        summary.put("reviewCount", count);
        summary.put("productId", productId);
        return summary;
    }

    @Transactional
    public void deleteReview(User user, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));
        if (!review.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You can only delete your own reviews.");
        }
        reviewRepository.delete(review);
    }
}
