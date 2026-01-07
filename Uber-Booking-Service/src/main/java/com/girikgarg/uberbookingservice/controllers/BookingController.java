package com.girikgarg.uberbookingservice.controllers;

import com.girikgarg.uberbookingservice.dto.CreateBookingDto;
import com.girikgarg.uberbookingservice.dto.CreateBookingResponseDto;
import com.girikgarg.uberbookingservice.services.api.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for booking operations.
 * 
 * End-to-End Flow:
 * 1. Passenger sends POST request with startLocation, endLocation, passengerId
 * 2. Controller validates and delegates to BookingService
 * 3. BookingService:
 *    a. Validates passenger exists
 *    b. Creates booking with ASSIGNING_DRIVER status
 *    c. Calls Location Service to fetch nearby drivers (5km radius)
 *    d. (Future) Sends ride request to drivers via WebSocket
 *    e. (Future) Waits for driver acceptance
 * 4. Returns booking details with status
 */
@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {
    
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * Create a new booking request.
     * 
     * @param createBookingDto Contains passengerId, startLocation, endLocation
     * @return CreateBookingResponseDto with bookingId, status, and driver (if assigned)
     */
    @PostMapping
    public ResponseEntity<CreateBookingResponseDto> create(@RequestBody CreateBookingDto createBookingDto) {
        CreateBookingResponseDto response = bookingService.create(createBookingDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
