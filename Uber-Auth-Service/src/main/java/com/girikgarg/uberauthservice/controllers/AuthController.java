package com.girikgarg.uberauthservice.controllers;

import com.girikgarg.uberauthservice.dto.PassengerSigninRequestDto;
import com.girikgarg.uberauthservice.dto.PassengerSigninResponseDto;
import com.girikgarg.uberauthservice.dto.PassengerSignupRequestDto;
import com.girikgarg.uberauthservice.dto.PassengerSignupResponseDto;
import com.girikgarg.uberauthservice.services.api.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Auth Service controller for user authentication and registration.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
public class AuthController {
    
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    @PostMapping("/signup/passenger")
    public ResponseEntity<PassengerSignupResponseDto> signUp(@RequestBody PassengerSignupRequestDto passengerSignupRequestDto) {
        log.info("Received passenger signup request for email: {}", passengerSignupRequestDto.getEmail());
        PassengerSignupResponseDto responseDto = authService.signupPassenger(passengerSignupRequestDto);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }
    
    @PostMapping("/signin/passenger")
    public ResponseEntity<PassengerSigninResponseDto> signIn(@RequestBody PassengerSigninRequestDto passengerSigninRequestDto) {
        log.info("Received passenger signin request for email: {}", passengerSigninRequestDto.getEmail());
        PassengerSigninResponseDto responseDto = authService.signinPassenger(passengerSigninRequestDto);
        return ResponseEntity.ok(responseDto);
    }
}
