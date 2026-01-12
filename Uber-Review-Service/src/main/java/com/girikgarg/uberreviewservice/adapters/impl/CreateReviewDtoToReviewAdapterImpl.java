package com.girikgarg.uberreviewservice.adapters.impl;

import com.girikgarg.uberentityservice.models.Booking;
import com.girikgarg.uberentityservice.models.Review;
import com.girikgarg.uberreviewservice.adapters.api.CreateReviewDtoToReviewAdapter;
import com.girikgarg.uberreviewservice.dtos.CreateReviewRequestDto;
import com.girikgarg.uberreviewservice.exceptions.BookingNotFoundException;
import com.girikgarg.uberreviewservice.repositories.BookingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Adapter implementation to convert CreateReviewRequestDto to Review entity.
 * Follows the Adapter design pattern to separate DTO-to-Entity conversion logic.
 */
@Component
@Slf4j
public class CreateReviewDtoToReviewAdapterImpl implements CreateReviewDtoToReviewAdapter {
    
    private final BookingRepository bookingRepository;

    public CreateReviewDtoToReviewAdapterImpl(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Override
    public Review convert(CreateReviewRequestDto requestDto) {
        log.debug("Converting CreateReviewRequestDto to Review entity for booking ID: {}", requestDto.getBookingId());
        
        // Fetch and validate booking exists
        Booking booking = bookingRepository.findById(requestDto.getBookingId())
                .orElseThrow(() -> new BookingNotFoundException(requestDto.getBookingId()));
        
        // Build Review entity
        Review review = Review.builder()
                .content(requestDto.getContent())
                .rating(requestDto.getRating())
                .booking(booking)
                .build();
        
        log.debug("Successfully converted DTO to Review entity");
        return review;
    }
}
