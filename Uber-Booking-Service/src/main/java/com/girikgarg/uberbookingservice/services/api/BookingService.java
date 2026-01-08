package com.girikgarg.uberbookingservice.services.api;

import com.girikgarg.uberbookingservice.dto.CreateBookingDto;
import com.girikgarg.uberbookingservice.dto.CreateBookingResponseDto;
import com.girikgarg.uberbookingservice.dto.UpdateBookingRequestDto;
import com.girikgarg.uberbookingservice.dto.UpdateBookingResponseDto;

public interface BookingService {
    CreateBookingResponseDto create(CreateBookingDto bookingDetails);

    UpdateBookingResponseDto update(UpdateBookingRequestDto requestDto, Long bookingId);
}
