package com.girikgarg.uberreviewservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * DTO for review response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDto {
    
    private Long id;
    private String content;
    private Double rating;
    private Long bookingId;
    private Date createdAt;
    private Date updatedAt;
}
