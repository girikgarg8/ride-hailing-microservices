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
public class ValidateTokenRequestDto {
    private Role requiredRole;
}
