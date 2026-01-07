package com.girikgarg.uberbookingservice.dto;

import lombok.*;

/**
 * DTO for requesting nearby drivers from Location Service.
 * Matches the contract of Location Service API.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NearbyDriversRequestDto {
    private Double latitude;
    private Double longitude;
}

