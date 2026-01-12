package com.girikgarg.uberreviewservice.repositories;

import com.girikgarg.uberentityservice.models.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    // Find all reviews for a specific booking
    List<Review> findAllByBookingId(Long bookingId);
    
    // Find a review by booking ID
    Optional<Review> findByBookingId(Long bookingId);
    
    // Find reviews by rating greater than or equal to threshold
    List<Review> findByRatingGreaterThanEqual(Double rating);
}
