package com.girikgarg.uberbookingservice.dto;

import com.girikgarg.uberentityservice.models.GeoCoordinates;
import lombok.*;

/**
 * DTO for creating a new booking request.
 * Contains passenger information and trip coordinates.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBookingDto {
    private Long passengerId;
    private GeoCoordinates startLocation;
    private GeoCoordinates endLocation; 
}
