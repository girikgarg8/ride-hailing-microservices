package com.girikgarg.uberbookingservice.dto;

import com.girikgarg.uberentityservice.models.BookingStatus;
import lombok.*;

import java.util.Optional;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBookingRequestDto {
    private BookingStatus status;
    private Optional<Long> driverId;
}
