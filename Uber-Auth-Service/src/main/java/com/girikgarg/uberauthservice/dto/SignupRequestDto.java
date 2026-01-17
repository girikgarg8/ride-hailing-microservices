package com.girikgarg.uberauthservice.dto;

import com.girikgarg.uberentityservice.models.Role;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequestDto {
    private String email;
    private String password;
    private String phoneNumber;
    private String name;
    private Role role; // DRIVER or PASSENGER
}
