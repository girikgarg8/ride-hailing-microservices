package com.girikgarg.uberbookingservice.dto;

import lombok.*;

/**
 * DTO representing a driver's location.
 * Matches the response from Location Service API.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverLocationDto {
    private String driverId;
    private Double latitude;
    private Double longitude;
}

