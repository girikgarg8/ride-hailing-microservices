package com.girikgarg.uberreviewservice.controllers;

import com.girikgarg.uberentityservice.models.Review;
import com.girikgarg.uberreviewservice.dtos.CreateReviewRequestDto;
import com.girikgarg.uberreviewservice.dtos.ReviewResponseDto;
import com.girikgarg.uberreviewservice.services.api.ReviewService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for review operations.
 * 
 * Endpoints:
 * - POST /api/v1/reviews - Create a new review
 * - GET /api/v1/reviews - Retrieve all reviews
 * - GET /api/v1/reviews/{reviewId} - Retrieve a specific review by ID
 * - DELETE /api/v1/reviews/{reviewId} - Delete a review by ID
 */
@RestController
@RequestMapping("/api/v1/reviews")
@Slf4j
public class ReviewController {
    
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * Create a new review for a booking.
     * 
     * @param requestDto Contains bookingId, content, and rating
     * @return ReviewResponseDto with created review details and 201 CREATED status
     * @throws BookingNotFoundException if the booking is not found
     */
    @PostMapping
    public ResponseEntity<ReviewResponseDto> createReview(@Valid @RequestBody CreateReviewRequestDto requestDto) {
        log.info("Received request to create review for booking ID: {}", requestDto.getBookingId());
        
        Review review = reviewService.createReview(requestDto);
        
        ReviewResponseDto response = ReviewResponseDto.builder()
                .id(review.getId())
                .content(review.getContent())
                .rating(review.getRating())
                .bookingId(review.getBooking().getId())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
        
        log.info("Successfully created review with ID: {}", review.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Retrieve all reviews from the system.
     * 
     * @return List of all reviews with 200 OK status
     */
    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews() {
        log.info("Received request to fetch all reviews");
        List<Review> reviews = reviewService.findAllReviews();
        log.info("Successfully retrieved {} reviews", reviews.size());
        return new ResponseEntity<>(reviews, HttpStatus.OK);
    }

    /**
     * Retrieve a specific review by its ID.
     * 
     * @param reviewId The ID of the review to retrieve
     * @return Review object with 200 OK if found, 404 NOT_FOUND if not found
     */
    @GetMapping("/{reviewId}")
    public ResponseEntity<?> getReviewById(@PathVariable Long reviewId) {
        log.info("Received request to fetch review with ID: {}", reviewId);
        
        Optional<Review> reviewOptional = reviewService.findReviewById(reviewId);
        
        if (reviewOptional.isEmpty()) {
            log.warn("Review not found with ID: {}", reviewId);
            return new ResponseEntity<>("Review not found with ID: " + reviewId, HttpStatus.NOT_FOUND);
        }
        
        Review review = reviewOptional.get();
        log.info("Successfully retrieved review with ID: {}. Rating: {}", reviewId, review.getRating());
        return new ResponseEntity<>(review, HttpStatus.OK);
    }

    /**
     * Delete a review by its ID.
     * 
     * @param reviewId The ID of the review to delete
     * @return Success message with 200 OK if deleted, 404 NOT_FOUND if review doesn't exist
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<String> deleteReviewById(@PathVariable Long reviewId) {
        log.info("Received request to delete review with ID: {}", reviewId);
        
        boolean isDeleted = reviewService.deleteReviewById(reviewId);
        
        if (!isDeleted) {
            log.warn("Unable to delete review with ID: {}. Review may not exist.", reviewId);
            return new ResponseEntity<>("Unable to delete review. Review may not exist.", HttpStatus.NOT_FOUND);
        }
        
        log.info("Successfully deleted review with ID: {}", reviewId);
        return new ResponseEntity<>("Review deleted successfully", HttpStatus.OK);
    }
}
