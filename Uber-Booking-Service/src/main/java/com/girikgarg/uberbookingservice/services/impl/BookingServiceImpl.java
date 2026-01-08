package com.girikgarg.uberbookingservice.services.impl;

import com.girikgarg.uberbookingservice.apis.LocationServiceApi;
import com.girikgarg.uberbookingservice.configuration.BookingServiceProperties;
import com.girikgarg.uberbookingservice.dto.CreateBookingDto;
import com.girikgarg.uberbookingservice.dto.CreateBookingResponseDto;
import com.girikgarg.uberbookingservice.dto.DriverLocationDto;
import com.girikgarg.uberbookingservice.dto.NearbyDriversRequestDto;
import com.girikgarg.uberbookingservice.dto.UpdateBookingRequestDto;
import com.girikgarg.uberbookingservice.dto.UpdateBookingResponseDto;
import com.girikgarg.uberbookingservice.repositories.BookingRepository;
import com.girikgarg.uberbookingservice.repositories.DriverRepository;
import com.girikgarg.uberbookingservice.repositories.PassengerRepository;
import com.girikgarg.uberbookingservice.services.api.BookingService;
import com.girikgarg.uberentityservice.models.Booking;
import com.girikgarg.uberentityservice.models.BookingStatus;
import com.girikgarg.uberentityservice.models.Driver;
import com.girikgarg.uberentityservice.models.Passenger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    private final DriverRepository driverRepository;
    private final RestTemplate restTemplate;
    private final BookingServiceProperties properties;
    private final LocationServiceApi locationServiceApi;

    public BookingServiceImpl(PassengerRepository passengerRepository, 
                              BookingRepository bookingRepository,
                              DriverRepository driverRepository,
                              RestTemplate restTemplate,
                              BookingServiceProperties properties,
                              LocationServiceApi locationServiceApi) {
        this.passengerRepository = passengerRepository;
        this.bookingRepository = bookingRepository;
        this.driverRepository = driverRepository;
        this.restTemplate = restTemplate;
        this.properties = properties;
        this.locationServiceApi = locationServiceApi;
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

        // 3. Call Location Service to fetch nearby drivers asynchronously using Retrofit
        NearbyDriversRequestDto request = NearbyDriversRequestDto.builder()
                .latitude(bookingDetails.getStartLocation().getLatitude())
                .longitude(bookingDetails.getStartLocation().getLongitude())
                .build();

        log.info("Fetching nearby drivers for location: ({}, {})", 
                request.getLatitude(), request.getLongitude());
        
        // Async call - won't block booking creation
        processNearbyDriversAsync(request);

        // 4. Return booking response (driver will be null until assigned)
        return CreateBookingResponseDto.builder()
                .bookingId(savedBooking.getId())
                .bookingStatus(savedBooking.getBookingStatus())
                .driver(Optional.ofNullable(savedBooking.getDriver()))
                .build();
    }

    /**
     * Update an existing booking.
     * Typically called when a driver accepts a ride request.
     * 
     * Flow:
     * 1. Validate booking exists
     * 2. Validate driver exists (if provided)
     * 3. Update booking status and driver
     * 4. Save and return updated booking
     */
    @Override
    public UpdateBookingResponseDto update(UpdateBookingRequestDto requestDto, Long bookingId) {
        log.info("Updating booking {} with status: {}", bookingId, requestDto.getStatus());
        
        // 1. Validate booking exists
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            log.error("Booking not found: {}", bookingId);
            throw new RuntimeException("Booking not found with ID: " + bookingId);
        }
        
        Booking booking = bookingOpt.get();
        log.info("Found booking: {} with current status: {}", booking.getId(), booking.getBookingStatus());
        
        // 2. Update booking status
        booking.setBookingStatus(requestDto.getStatus());
        
        // 3. Update driver if provided
        if (requestDto.getDriverId() != null && requestDto.getDriverId().isPresent()) {
            Long driverId = requestDto.getDriverId().get();
            log.info("Assigning driver {} to booking {}", driverId, bookingId);
            
            Optional<Driver> driverOpt = driverRepository.findById(driverId);
            if (driverOpt.isEmpty()) {
                log.error("Driver not found: {}", driverId);
                throw new RuntimeException("Driver not found with ID: " + driverId);
            }
            
            Driver driver = driverOpt.get();
            booking.setDriver(driver);
            log.info("Driver {} assigned to booking {}", driver.getId(), booking.getId());
        } else {
            log.info("No driver change requested for booking {}", bookingId);
        }
        
        // 4. Save updated booking
        Booking updatedBooking = bookingRepository.save(booking);
        log.info("Booking {} updated successfully with status: {}", 
                updatedBooking.getId(), updatedBooking.getBookingStatus());
        
        // 5. Return response
        return UpdateBookingResponseDto.builder()
                .bookingId(updatedBooking.getId())
                .status(updatedBooking.getBookingStatus())
                .driver(Optional.ofNullable(updatedBooking.getDriver()))
                .build();
    }

    private void processNearbyDriversAsync(NearbyDriversRequestDto requestDto) {
        Call<DriverLocationDto[]> call = locationServiceApi.getNearbyDrivers(requestDto);
        call.enqueue(new Callback<DriverLocationDto[]>() {
            @Override
            public void onResponse(Call<DriverLocationDto[]> call, Response<DriverLocationDto[]> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DriverLocationDto[] nearbyDrivers = response.body();
                    log.info("Found {} nearby drivers", nearbyDrivers.length);
                    
                    // Log available drivers (Future: Send ride request via WebSocket)
                    for (DriverLocationDto driver : nearbyDrivers) {
                        log.info("Driver {} at location: ({}, {})", 
                                driver.getDriverId(), 
                                driver.getLatitude(), 
                                driver.getLongitude());
                    }
                }
            }

            @Override
            public void onFailure(Call<DriverLocationDto[]> call, Throwable t) {
                log.error("Failed to fetch nearby drivers: {}", t.getMessage(), t);
            }
        });
    }
}
