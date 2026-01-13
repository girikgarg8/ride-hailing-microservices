package com.girikgarg.uberauthservice.dto;

import com.girikgarg.uberentityservice.models.Passenger;
import lombok.*;
import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PassengerSignupResponseDto {
    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
    private Date createdAt;
    private String password; // hashed password

    public static PassengerSignupResponseDto from(Passenger passenger) {
        return PassengerSignupResponseDto.builder()
                .id(passenger.getId())
                .name(passenger.getName())
                .email(passenger.getEmail())
                .phoneNumber(passenger.getPhoneNumber())
                .createdAt(passenger.getCreatedAt())
                .password(passenger.getPassword())
                .build();
    }
}
