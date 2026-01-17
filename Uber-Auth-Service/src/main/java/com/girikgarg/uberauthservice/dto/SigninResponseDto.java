package com.girikgarg.uberauthservice.dto;

import com.girikgarg.uberentityservice.models.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SigninResponseDto {
    private Boolean success;
    private Long id;
    private String email;
    private Role role;
}
