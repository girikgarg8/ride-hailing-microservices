package com.girikgarg.uberbookingservice.dto;

import com.girikgarg.uberentityservice.models.BookingStatus;
import lombok.*;

/**
 * DTO for ride request sent to Socket Service for broadcasting to drivers.
 * Contains essential booking information needed by drivers.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RideRequestDto {
    private Long bookingId;
    private Long passengerId;
    private Double startLatitude;
    private Double startLongitude;
    private Double endLatitude;
    private Double endLongitude;
    private BookingStatus bookingStatus;
}

