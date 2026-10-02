package com.clickkart.controller;

import com.clickkart.dto.response.ApiResponse;
import com.clickkart.dto.response.ReviewResponse;
import com.clickkart.entity.User;
import com.clickkart.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * POST /api/reviews/{productId}
     * Body: { "rating": 5, "comment": "Great product!" }
     */
    @PostMapping("/{productId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> addReview(
            @AuthenticationPrincipal User user,
            @PathVariable Long productId,
            @RequestBody Map<String, Object> body) {

        Integer rating = (Integer) body.get("rating");
        String comment = (String) body.get("comment");

        ReviewResponse review = reviewService.addReview(user, productId, rating, comment);
        return ResponseEntity.ok(ApiResponse.success("Review submitted successfully!", review));
    }

    /**
     * GET /api/reviews/{productId}
     * Returns all reviews for a product (public).
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviews(
            @PathVariable Long productId) {
        List<ReviewResponse> reviews = reviewService.getReviewsByProduct(productId);
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }

    /**
     * GET /api/reviews/{productId}/summary
     * Returns { averageRating, reviewCount, productId } (public).
     */
    @GetMapping("/{productId}/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getReviewSummary(
            @PathVariable Long productId) {
        Map<String, Object> summary = reviewService.getReviewSummary(productId);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    /**
     * DELETE /api/reviews/{reviewId}
     * User can delete their own review.
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @AuthenticationPrincipal User user,
            @PathVariable Long reviewId) {
        reviewService.deleteReview(user, reviewId);
        return ResponseEntity.ok(ApiResponse.success("Review deleted.", null));
    }
}
