package com.girikgarg.uberbookingservice.dto;

import com.girikgarg.uberentityservice.models.BookingStatus;
import com.girikgarg.uberentityservice.models.Driver;
import lombok.*;

import java.util.Optional;

/**
 * DTO for booking creation response.
 * Returns booking ID, status, and assigned driver (if any).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBookingResponseDto {
    private Long bookingId;
    private BookingStatus bookingStatus;
    private Optional<Driver> driver;
}
