package com.girikgarg.uberbookingservice.consumers;

import com.girikgarg.uberbookingservice.repositories.BookingRepository;
import com.girikgarg.uberbookingservice.repositories.DriverRepository;
import com.girikgarg.uberentityservice.models.Booking;
import com.girikgarg.uberentityservice.models.BookingStatus;
import com.girikgarg.uberentityservice.models.Driver;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Kafka Consumer Service for Booking Service.
 * Consumes messages from Kafka topics to handle ride-related events.
 */
@Service
@Slf4j
public class KafkaConsumerService {

    private final BookingRepository bookingRepository;
    private final DriverRepository driverRepository;
    private final Gson gson;

    public KafkaConsumerService(BookingRepository bookingRepository, DriverRepository driverRepository) {
        this.bookingRepository = bookingRepository;
        this.driverRepository = driverRepository;
        this.gson = new Gson();
    }

    /**
     * Consume messages from the sample topic.
     * This demonstrates inter-service communication via Kafka.
     * 
     * @param message The message consumed from Kafka
     */
    @KafkaListener(topics = "sample", groupId = "booking-service-group")
    public void consumeRideEvents(String message) {
        log.info("Kafka message consumed from topic 'sample' in Booking Service: {}", message);
        // Process the event here (e.g., send notifications, update analytics, trigger workflows)
    }
    
    /**
     * Consume ride acceptance events from Socket Service.
     * When a driver accepts a ride, this method updates the booking in the database.
     * 
     * @param message JSON message containing bookingId and driverId
     */
    @KafkaListener(topics = "ride-accepted", groupId = "booking-service-group")
    @Transactional
    public void consumeRideAcceptedEvent(String message) {
        log.info("Received ride accepted event from Kafka: {}", message);
        
        try {
            // Parse JSON message
            JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
            Long bookingId = jsonObject.get("bookingId").getAsLong();
            Long driverId = jsonObject.get("driverId").getAsLong();
            
            log.info("Processing ride acceptance. Booking ID: {}, Driver ID: {}", bookingId, driverId);
            
            // Find booking
            Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
            
            if (bookingOptional.isEmpty()) {
                log.error("Booking not found with ID: {}", bookingId);
                return;
            }
            
            // Find driver
            Optional<Driver> driverOptional = driverRepository.findById(driverId);
            
            if (driverOptional.isEmpty()) {
                log.error("Driver not found with ID: {}", driverId);
                return;
            }
            
            // Update booking with driver and status
            Booking booking = bookingOptional.get();
            Driver driver = driverOptional.get();
            
            booking.setBookingStatus(BookingStatus.SCHEDULED);
            booking.setDriver(driver);
            
            bookingRepository.save(booking);
            
            log.info("Booking updated successfully. Booking ID: {}, Status: SCHEDULED, Driver: {} (ID: {})", 
                    bookingId, driver.getName(), driver.getId());
            
        } catch (Exception e) {
            log.error("Error processing ride accepted event. Message: {}, Error: {}", 
                    message, e.getMessage(), e);
        }
    }
}

