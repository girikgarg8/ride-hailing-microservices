package com.girikgarg.uberauthservice.services.api;

import com.girikgarg.uberauthservice.dto.PassengerSignupRequestDto;
import com.girikgarg.uberauthservice.dto.PassengerSignupResponseDto;

public interface AuthService {
    PassengerSignupResponseDto signupPassenger(PassengerSignupRequestDto passengerSignupRequestDto);
}
