package com.girikgarg.uberclientsocketservice.dto;

import lombok.*;

/**
 * DTO for driver's response to a ride request.
 * Sent when driver accepts or rejects a ride.
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RideResponseDto {
    private Boolean response;  // true = accepted, false = rejected
    private Long bookingId;
    private Long driverId;     // ID of the driver responding
}

