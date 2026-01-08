package com.girikgarg.uberbookingservice.controllers;

import com.girikgarg.uberbookingservice.dto.CreateBookingDto;
import com.girikgarg.uberbookingservice.dto.CreateBookingResponseDto;
import com.girikgarg.uberbookingservice.dto.UpdateBookingRequestDto;
import com.girikgarg.uberbookingservice.dto.UpdateBookingResponseDto;
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

    /**
     * Update an existing booking.
     * Used by drivers to accept rides or update booking status.
     * 
     * End-to-End Flow:
     * 1. Driver accepts ride via WebSocket notification
     * 2. Socket Service calls this endpoint with driverId and new status
     * 3. BookingService:
     *    a. Validates booking exists
     *    b. Validates driver exists (if provided)
     *    c. Updates booking status and assigns driver
     *    d. Saves updated booking
     * 4. Returns updated booking details
     * 
     * @param requestDto Contains status and optional driverId
     * @param bookingId The ID of the booking to update
     * @return UpdateBookingResponseDto with updated booking details
     */
    @PatchMapping("/{bookingId}")
    public ResponseEntity<UpdateBookingResponseDto> updateBooking(
            @RequestBody UpdateBookingRequestDto requestDto, 
            @PathVariable Long bookingId) {
        UpdateBookingResponseDto response = bookingService.update(requestDto, bookingId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
