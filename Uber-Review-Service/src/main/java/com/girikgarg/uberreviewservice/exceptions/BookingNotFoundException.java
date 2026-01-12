package com.girikgarg.uberreviewservice.exceptions;

/**
 * Exception thrown when a booking is not found.
 */
public class BookingNotFoundException extends RuntimeException {
    
    public BookingNotFoundException(Long bookingId) {
        super("Booking not found with ID: " + bookingId);
    }
    
    public BookingNotFoundException(String message) {
        super(message);
    }
}
