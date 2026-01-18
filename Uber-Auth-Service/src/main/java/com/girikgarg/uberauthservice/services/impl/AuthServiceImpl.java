package com.girikgarg.uberauthservice.services.impl;

import com.girikgarg.uberauthservice.dto.SignupRequestDto;
import com.girikgarg.uberauthservice.dto.SignupResponseDto;
import com.girikgarg.uberauthservice.repositories.DriverRepository;
import com.girikgarg.uberauthservice.repositories.PassengerRepository;
import com.girikgarg.uberauthservice.services.api.AuthService;
import com.girikgarg.uberentityservice.models.Driver;
import com.girikgarg.uberentityservice.models.DriverApprovalStatus;
import com.girikgarg.uberentityservice.models.Passenger;
import com.girikgarg.uberentityservice.models.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final DriverRepository driverRepository;
    private final PassengerRepository passengerRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(DriverRepository driverRepository, 
        PassengerRepository passengerRepository, 
                          PasswordEncoder passwordEncoder) {
        this.driverRepository = driverRepository;
        this.passengerRepository = passengerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public SignupResponseDto signup(SignupRequestDto signupRequestDto) {
        log.info("Attempting to signup {} with email: {}", 
                 signupRequestDto.getRole(), 
                 signupRequestDto.getEmail());
        
        String hashedPassword = passwordEncoder.encode(signupRequestDto.getPassword());
        
        // Create Driver or Passenger record directly based on role
        if (signupRequestDto.getRole() == Role.DRIVER) {
            Driver driver = Driver.builder()
                    .name(signupRequestDto.getName())
                    .email(signupRequestDto.getEmail())
                    .password(hashedPassword)
                    .phoneNumber(signupRequestDto.getPhoneNumber())
                    .driverApprovalStatus(DriverApprovalStatus.APPROVED)
                    .rating(5.0)
                    .isAvailable(true)
                    .build();
            
            Driver savedDriver = driverRepository.save(driver);
            log.info("Driver signed up successfully with ID: {}", savedDriver.getId());
            
            return SignupResponseDto.builder()
                    .id(savedDriver.getId())
                    .name(savedDriver.getName())
                    .email(savedDriver.getEmail())
                    .phoneNumber(savedDriver.getPhoneNumber())
                    .role(Role.DRIVER)
                    .createdAt(savedDriver.getCreatedAt())
                    .build();
                    
        } else if (signupRequestDto.getRole() == Role.PASSENGER) {
        Passenger passenger = Passenger.builder()
                    .name(signupRequestDto.getName())
                    .email(signupRequestDto.getEmail())
                    .password(hashedPassword)
                    .phoneNumber(signupRequestDto.getPhoneNumber())
                    .rating(5.0)
                .build();

            Passenger savedPassenger = passengerRepository.save(passenger);
            log.info("Passenger signed up successfully with ID: {}", savedPassenger.getId());
        
            return SignupResponseDto.builder()
                    .id(savedPassenger.getId())
                    .name(savedPassenger.getName())
                    .email(savedPassenger.getEmail())
                    .phoneNumber(savedPassenger.getPhoneNumber())
                    .role(Role.PASSENGER)
                    .createdAt(savedPassenger.getCreatedAt())
                    .build();
        } else {
            throw new RuntimeException("Unsupported role: " + signupRequestDto.getRole());
        }
    }
}
