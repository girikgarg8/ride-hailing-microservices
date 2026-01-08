package com.girikgarg.uberbookingservice.dto;

import com.girikgarg.uberentityservice.models.BookingStatus;
import com.girikgarg.uberentityservice.models.Driver;
import lombok.*;

import java.util.Optional;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBookingResponseDto {
    private Long bookingId;
    private BookingStatus status;
    private Optional<Driver> driver;
}
