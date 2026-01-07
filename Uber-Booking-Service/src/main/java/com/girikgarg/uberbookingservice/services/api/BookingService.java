package com.girikgarg.uberbookingservice.services.api;

import com.girikgarg.uberbookingservice.dto.CreateBookingDto;
import com.girikgarg.uberbookingservice.dto.CreateBookingResponseDto;

public interface BookingService {
    CreateBookingResponseDto create(CreateBookingDto bookingDetails);
}
