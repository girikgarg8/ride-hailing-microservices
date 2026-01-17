package com.girikgarg.uberauthservice.controllers;

import com.girikgarg.uberauthservice.dto.SigninRequestDto;
import com.girikgarg.uberauthservice.dto.SigninResponseDto;
import com.girikgarg.uberauthservice.dto.SignupRequestDto;
import com.girikgarg.uberauthservice.dto.SignupResponseDto;
import com.girikgarg.uberauthservice.dto.ValidateTokenRequestDto;
import com.girikgarg.uberauthservice.helpers.AuthUserDetails;
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
    
    @PostMapping("/signup")
    public ResponseEntity<SignupResponseDto> signUp(@RequestBody SignupRequestDto request) {
        log.info("Signup request: email={}, role={}", request.getEmail(), request.getRole());
        SignupResponseDto response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/signin")
    public ResponseEntity<SigninResponseDto> signIn(@RequestBody SigninRequestDto request, HttpServletResponse response) {
        log.info("Signin request: email={}", request.getEmail());
        
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        
        AuthUserDetails userDetails = (AuthUserDetails) authentication.getPrincipal();
        log.info("Authenticated: email={}, role={}", request.getEmail(), userDetails.getRole());
        
        String jwtToken = jwtUtil.createToken(request.getEmail());
        
        ResponseCookie cookie = ResponseCookie.from("JwtToken", jwtToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(cookieExpiry)
                .build();
        
        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        
        SigninResponseDto signinResponse = SigninResponseDto.builder()
                .success(true)
                .id(userDetails.getId())
                .email(request.getEmail())
                .role(userDetails.getRole())
                .build();
        
        return ResponseEntity.ok(signinResponse);
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestBody ValidateTokenRequestDto validateRequest, HttpServletRequest request) {
        
        // Expected role is mandatory
        if (validateRequest == null || validateRequest.getRequiredRole() == null) {
            log.error("Validation failed: missing expected role");
            return ResponseEntity.badRequest().body(java.util.Map.of(
                "valid", false,
                "message", "Expected role must be specified"
            ));
        }
        
        String userEmail = getAttributeAsString(request, "email", "Unknown");
        String userRole = getAttributeAsString(request, "role", null);
        String expectedRole = validateRequest.getRequiredRole().name();
        
        // Check if user's role matches expected role
        if (userRole == null || !userRole.equals(expectedRole)) {
            log.warn("Authorization failed: user={}, userRole={}, expectedRole={}", userEmail, userRole, expectedRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(java.util.Map.of(
                "valid", false,
                "email", userEmail,
                "userRole", userRole != null ? userRole : "UNKNOWN",
                "expectedRole", expectedRole,
                "message", "User does not have the required role"
            ));
        }
        
        log.info("Validated: email={}, role={}", userEmail, userRole);
        
        return ResponseEntity.ok(java.util.Map.of(
            "valid", true,
            "email", userEmail,
            "role", userRole,
            "message", "Token is valid and role matches"
        ));
    }
    
    private String getAttributeAsString(HttpServletRequest request, String attributeName, String defaultValue) {
        Object attribute = request.getAttribute(attributeName);
        return attribute != null ? attribute.toString() : defaultValue;
    }
}
