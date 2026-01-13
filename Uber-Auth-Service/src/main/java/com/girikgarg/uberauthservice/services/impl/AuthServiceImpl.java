package com.girikgarg.uberauthservice.services.impl;

import com.girikgarg.uberauthservice.dto.PassengerSignupRequestDto;
import com.girikgarg.uberauthservice.dto.PassengerSignupResponseDto;
import com.girikgarg.uberauthservice.repositories.PassengerRepository;
import com.girikgarg.uberauthservice.services.api.AuthService;
import com.girikgarg.uberentityservice.models.Passenger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final PassengerRepository passengerRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(PassengerRepository passengerRepository, PasswordEncoder passwordEncoder) {
        this.passengerRepository = passengerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public PassengerSignupResponseDto signupPassenger(PassengerSignupRequestDto passengerSignupRequestDto) {
        log.info("Attempting to signup passenger with email: {}", passengerSignupRequestDto.getEmail());
        
        Passenger passenger = Passenger.builder()
                .email(passengerSignupRequestDto.getEmail())
                .name(passengerSignupRequestDto.getName())
                .password(passwordEncoder.encode(passengerSignupRequestDto.getPassword()))
                .phoneNumber(passengerSignupRequestDto.getPhoneNumber())
                .build();

        Passenger newPassenger = passengerRepository.save(passenger);
        log.info("Passenger signed up successfully with ID: {}", newPassenger.getId());
        
        return PassengerSignupResponseDto.from(newPassenger);
    }
}
