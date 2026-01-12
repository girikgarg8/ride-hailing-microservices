package com.girikgarg.uberreviewservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * Standard error response DTO for API errors.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDto {
    
    private Date timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private List<String> errors; // For validation errors
}
