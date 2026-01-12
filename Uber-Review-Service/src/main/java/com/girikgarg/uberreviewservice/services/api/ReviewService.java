package com.girikgarg.uberreviewservice.services.api;

import com.girikgarg.uberentityservice.models.Review;
import com.girikgarg.uberreviewservice.dtos.CreateReviewRequestDto;

import java.util.List;
import java.util.Optional;

public interface ReviewService {
    Review createReview(CreateReviewRequestDto requestDto);

    Optional<Review> findReviewById(Long id);

    List<Review> findAllReviews();

    boolean deleteReviewById(Long id);
}  
