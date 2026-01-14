package com.girikgarg.uberauthservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PassengerSigninResponseDto {
    private String token;
    private String tokenType;
    private int expiresIn;
}
