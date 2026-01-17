package com.girikgarg.uberauthservice.services.api;

import com.girikgarg.uberauthservice.dto.SignupRequestDto;
import com.girikgarg.uberauthservice.dto.SignupResponseDto;

public interface AuthService {
    SignupResponseDto signup(SignupRequestDto signupRequestDto);
}
