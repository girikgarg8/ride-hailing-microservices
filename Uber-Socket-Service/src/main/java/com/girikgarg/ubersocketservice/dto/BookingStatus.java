package com.girikgarg.ubersocketservice.dto;

/**
 * Status of a booking in the ride-hailing system.
 * Local enum for Socket Service (no entity dependencies).
 */
public enum BookingStatus {
    ASSIGNING_DRIVER,
    SCHEDULED,
    CAB_ARRIVED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

