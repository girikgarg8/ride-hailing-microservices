package com.girikgarg.uberreviewservice.services.impl;

import com.girikgarg.uberentityservice.models.Review;
import com.girikgarg.uberreviewservice.adapters.api.CreateReviewDtoToReviewAdapter;
import com.girikgarg.uberreviewservice.dtos.CreateReviewRequestDto;
import com.girikgarg.uberreviewservice.repositories.ReviewRepository;
import com.girikgarg.uberreviewservice.services.api.ReviewService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ReviewServiceImpl implements ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final CreateReviewDtoToReviewAdapter reviewAdapter;

    public ReviewServiceImpl(ReviewRepository reviewRepository, 
                            CreateReviewDtoToReviewAdapter reviewAdapter) {
        this.reviewRepository = reviewRepository;
        this.reviewAdapter = reviewAdapter;
    }

    @Override
    @Transactional
    public Review createReview(CreateReviewRequestDto requestDto) {
        log.info("Creating review for booking ID: {}", requestDto.getBookingId());
        
        // Use adapter to convert DTO to entity (includes booking validation)
        Review review = reviewAdapter.convert(requestDto);
        
        // Save review to database
        Review savedReview = reviewRepository.save(review);
        log.info("Successfully created review with ID: {} for booking ID: {}", 
                savedReview.getId(), requestDto.getBookingId());
        
        return savedReview;
    }

    @Override
    public Optional<Review> findReviewById(Long id) {
        log.info("Fetching review with ID: {}", id);
        return reviewRepository.findById(id);
    }
    
    @Override
    public List<Review> findAllReviews() {
        log.info("Fetching all reviews");
        return reviewRepository.findAll();
    }

    @Override
    public boolean deleteReviewById(Long id) {
        log.info("Attempting to delete review with ID: {}", id);
        try {
            // Check if review exists before deleting
            if (!reviewRepository.existsById(id)) {
                log.warn("Review with ID: {} does not exist", id);
                return false;
            }
            reviewRepository.deleteById(id);
            log.info("Successfully deleted review with ID: {}", id);
            return true;
        } catch (Exception ex) {
            log.error("Error deleting review with ID: {}. Error: {}", id, ex.getMessage());
            return false;
        }
    }
}
