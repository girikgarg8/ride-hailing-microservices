package com.girikgarg.uberbookingservice.services.impl;

import com.girikgarg.uberbookingservice.configuration.BookingServiceProperties;
import com.girikgarg.uberbookingservice.dto.CreateBookingDto;
import com.girikgarg.uberbookingservice.dto.CreateBookingResponseDto;
import com.girikgarg.uberbookingservice.dto.DriverLocationDto;
import com.girikgarg.uberbookingservice.dto.NearbyDriversRequestDto;
import com.girikgarg.uberbookingservice.repositories.BookingRepository;
import com.girikgarg.uberbookingservice.repositories.PassengerRepository;
import com.girikgarg.uberbookingservice.services.api.BookingService;
import com.girikgarg.uberentityservice.models.Booking;
import com.girikgarg.uberentityservice.models.BookingStatus;
import com.girikgarg.uberentityservice.models.Passenger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of BookingService.
 * Handles the complete booking flow including driver assignment.
 * 
 * Flow:
 * 1. Validate passenger exists
 * 2. Create booking with ASSIGNING_DRIVER status
 * 3. Call Location Service to get nearby drivers (within 5km)
 * 4. Log available drivers (Future: Send WebSocket requests)
 * 5. Return booking response
 */
@Service
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final PassengerRepository passengerRepository;
    private final BookingRepository bookingRepository;
    private final RestTemplate restTemplate;
    private final BookingServiceProperties properties;

    public BookingServiceImpl(PassengerRepository passengerRepository, 
                              BookingRepository bookingRepository,
                              RestTemplate restTemplate,
                              BookingServiceProperties properties) {
        this.passengerRepository = passengerRepository;
        this.bookingRepository = bookingRepository;
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    @Override
    public CreateBookingResponseDto create(CreateBookingDto bookingDetails) {
        log.info("Creating booking for passenger: {}", bookingDetails.getPassengerId());
        
        // 1. Validate passenger exists
        Optional<Passenger> passengerOpt = passengerRepository.findById(bookingDetails.getPassengerId());
        if (passengerOpt.isEmpty()) {
            log.error("Passenger not found: {}", bookingDetails.getPassengerId());
            throw new RuntimeException("Passenger not found with ID: " + bookingDetails.getPassengerId());
        }
        
        Passenger passenger = passengerOpt.get();
        log.info("Found passenger: {}", passenger.getId());

        // 2. Create booking with ASSIGNING_DRIVER status
        Booking booking = Booking.builder()
                .bookingStatus(BookingStatus.ASSIGNING_DRIVER)
                .startLocation(bookingDetails.getStartLocation())
                .endLocation(bookingDetails.getEndLocation())
                .passenger(passenger)
                .build();
        
        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking created with ID: {} and status: {}", savedBooking.getId(), savedBooking.getBookingStatus());

        // 3. Call Location Service to fetch nearby drivers
        NearbyDriversRequestDto request = NearbyDriversRequestDto.builder()
                .latitude(bookingDetails.getStartLocation().getLatitude())
                .longitude(bookingDetails.getStartLocation().getLongitude())
                .build();

        log.info("Fetching nearby drivers for location: ({}, {})", 
                request.getLatitude(), request.getLongitude());

        try {
            // Using exchange() for better type handling with Lists
            ResponseEntity<List<DriverLocationDto>> response = restTemplate.exchange(
                    properties.getLocationServiceUrl() + "/api/location/nearby/drivers",
                    HttpMethod.POST,
                    new org.springframework.http.HttpEntity<>(request),
                    new ParameterizedTypeReference<List<DriverLocationDto>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<DriverLocationDto> nearbyDrivers = response.getBody();
                log.info("Found {} nearby drivers", nearbyDrivers.size());
                
                // 4. Log available drivers (Future: Send ride request via WebSocket)
                nearbyDrivers.forEach(driver -> {
                    log.info("Driver {} at location: ({}, {})", 
                            driver.getDriverId(), 
                            driver.getLatitude(), 
                            driver.getLongitude());
                });
            } else {
                log.warn("No nearby drivers found or service error");
            }
        } catch (Exception ex) {
            log.error("Error calling Location Service: {}", ex.getMessage(), ex);
        }

        // 5. Return booking response (driver will be null until assigned)
        return CreateBookingResponseDto.builder()
                .bookingId(savedBooking.getId())
                .bookingStatus(savedBooking.getBookingStatus())
                .driver(Optional.ofNullable(savedBooking.getDriver()))
                .build();
    }
}
