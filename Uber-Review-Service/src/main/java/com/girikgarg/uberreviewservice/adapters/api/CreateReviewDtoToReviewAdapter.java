package com.girikgarg.uberreviewservice.adapters.api;

import com.girikgarg.uberentityservice.models.Review;
import com.girikgarg.uberreviewservice.dtos.CreateReviewRequestDto;

/**
 * Adapter interface for converting CreateReviewRequestDto to Review entity.
 * Implements the Adapter design pattern to decouple DTO-to-Entity conversion.
 */
public interface CreateReviewDtoToReviewAdapter {
    /**
     * Converts a CreateReviewRequestDto to a Review entity.
     * 
     * @param request The DTO containing review creation data
     * @return Review entity ready to be persisted
     * @throws BookingNotFoundException if the booking specified in the DTO doesn't exist
     */
    Review convert(CreateReviewRequestDto request);
}
