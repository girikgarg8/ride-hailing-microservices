package com.girikgarg.ubersocketservice.dto;

import lombok.*;

/**
 * DTO for ride request notifications sent to drivers via WebSocket.
 * Contains essential booking information for drivers to accept/reject.
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
