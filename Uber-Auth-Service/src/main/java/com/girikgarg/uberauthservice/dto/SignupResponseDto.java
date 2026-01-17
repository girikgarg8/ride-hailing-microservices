package com.girikgarg.uberauthservice.dto;

import com.girikgarg.uberentityservice.models.Role;
import lombok.*;
import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupResponseDto {
    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
    private Role role;
    private Date createdAt;
}
