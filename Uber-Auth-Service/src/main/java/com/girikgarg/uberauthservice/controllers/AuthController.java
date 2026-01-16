package com.girikgarg.uberauthservice.controllers;

import com.girikgarg.uberauthservice.dto.PassengerSigninRequestDto;
import com.girikgarg.uberauthservice.dto.PassengerSigninResponseDto;
import com.girikgarg.uberauthservice.dto.PassengerSignupRequestDto;
import com.girikgarg.uberauthservice.dto.PassengerSignupResponseDto;
import com.girikgarg.uberauthservice.services.api.AuthService;
import com.girikgarg.uberauthservice.utils.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Auth Service controller for user authentication and registration.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
public class AuthController {
    
    @Value("${cookie.expiry}")
    private int cookieExpiry;

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;

    public AuthController(AuthService authService, AuthenticationManager authenticationManager, JWTUtil jwtUtil) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }
    
    @PostMapping("/signup/passenger")
    public ResponseEntity<PassengerSignupResponseDto> signUp(@RequestBody PassengerSignupRequestDto passengerSignupRequestDto) {
        log.info("Received passenger signup request for email: {}", passengerSignupRequestDto.getEmail());
        PassengerSignupResponseDto responseDto = authService.signupPassenger(passengerSignupRequestDto);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }
    
    @PostMapping("/signin/passenger")
    public ResponseEntity<?> signIn(@RequestBody PassengerSigninRequestDto authRequestDto, HttpServletResponse response) {
        log.info("Received passenger signin request for email: {}", authRequestDto.getEmail());
        
        // creating an object of UsernamePasswordAuthenticationToken since it the DaoAuthenticationProvider supports it
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(authRequestDto.getEmail(), authRequestDto.getPassword())
        );
        
        if (authentication.isAuthenticated()) {
            log.info("Passenger authenticated successfully: {}", authRequestDto.getEmail());
            String jwtToken = jwtUtil.createToken(authRequestDto.getEmail());

            ResponseCookie cookie = ResponseCookie.from("JwtToken", jwtToken)
                                        .httpOnly(true)
                                        .secure(false)
                                        .path("/")
                                        .maxAge(cookieExpiry)
                                        .build();

            response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            log.info("JWT token created and set in cookie for email: {}", authRequestDto.getEmail());
            return new ResponseEntity<>(PassengerSigninResponseDto.builder().success(true).build(), HttpStatus.OK);
        } else {
            log.error("Authentication failed for email: {}", authRequestDto.getEmail());
            return new ResponseEntity<>("Something went wrong during authentication", HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(HttpServletRequest request) {
        log.info("Token validation request received");
        String email = request.getAttribute("email") != null ? 
                       request.getAttribute("email").toString() : "Unknown";
        log.info("Token validated successfully for user: {}", email);
        return new ResponseEntity<>("Token is valid", HttpStatus.OK);
    }
}
