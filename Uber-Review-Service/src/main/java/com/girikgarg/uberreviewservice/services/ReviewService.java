package com.girikgarg.uberreviewservice.services;

import com.girikgarg.uberentityservice.models.Booking;
import com.girikgarg.uberentityservice.models.Review;
import com.girikgarg.uberreviewservice.repositories.ReviewRepository;
import com.girikgarg.uberreviewservice.repositories.BookingRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService implements CommandLineRunner {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    
    @PersistenceContext
    private EntityManager entityManager;

    public ReviewService(ReviewRepository reviewRepository, BookingRepository bookingRepository) {
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("**************");
        
        // Need to fetch an existing booking since Review requires it
        Booking booking = bookingRepository.findById(1L).orElse(null);
        
        if (booking == null) {
            System.out.println("No booking found with ID 1. Skipping review demo.");
            return;
        }
        
        // Merge the booking to attach it to the current persistence context
        booking = entityManager.merge(booking);
        
        Review r = Review.builder()
                .content("Amazing ride quality")
                .rating(4.0)
                .build(); // code to create plain java object
        
        r.setBooking(booking); // Set the booking relationship
        
        System.out.println(r);
        
        reviewRepository.save(r); // this code executes SQL query
        
        System.out.println(r.getId());
        
        List<Review> reviews = reviewRepository.findAll();
        
        for(Review review : reviews) {
            System.out.println(review.getContent());
        }
        
        // reviewRepository.deleteById(2L);
    }
}

