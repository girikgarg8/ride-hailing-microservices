package com.girikgarg.ubersocketservice.controller;

import com.girikgarg.ubersocketservice.dto.RideRequestDto;
import com.girikgarg.ubersocketservice.dto.RideResponseDto;
import com.girikgarg.ubersocketservice.producers.KafkaProducerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for handling ride request notifications and driver responses.
 * 
 * End-to-End Flow:
 * 1. Booking Service creates a new booking
 * 2. After finding nearby drivers, Booking Service calls /newride
 * 3. Socket Service broadcasts to all connected drivers
 * 4. Driver receives notification in browser
 * 5. Driver clicks Accept/Reject
 * 6. Driver client calls /rideResponse endpoint
 * 7. Socket Service forwards response to Booking Service (future)
 */
@RestController
@RequestMapping("/api/socket")
@Slf4j
public class DriverRequestController {
    
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final RestTemplate restTemplate;
    private final KafkaProducerService kafkaProducerService;
    
    @Value("${booking.service.url:http://localhost:7475}")
    private String bookingServiceUrl;

    public DriverRequestController(SimpMessagingTemplate simpMessagingTemplate, 
                                   RestTemplate restTemplate,
                                   KafkaProducerService kafkaProducerService) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.restTemplate = restTemplate;
        this.kafkaProducerService = kafkaProducerService;
    }
    
    /**
     * Test endpoint to verify Kafka integration.
     * Publishes a test message to the 'sample' topic.
     * 
     * @return Success response
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> testKafka() {
        log.info("Test endpoint called - publishing message to Kafka");
        kafkaProducerService.publishMessage("sample", "Hello");
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Message 'Hello' published to 'sample' topic");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Test endpoint to simulate driver accepting a ride.
     * Directly publishes ride acceptance event to Kafka.
     * 
     * @param bookingId Booking ID
     * @param driverId Driver ID
     * @return Success response
     */
    @PostMapping("/test-ride-acceptance")
    public ResponseEntity<Map<String, String>> testRideAcceptance(
            @RequestParam Long bookingId,
            @RequestParam Long driverId) {
        log.info("Test ride acceptance endpoint called. Booking ID: {}, Driver ID: {}", bookingId, driverId);
        publishRideAcceptedEvent(bookingId, driverId);
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Ride acceptance event published to Kafka");
        response.put("bookingId", String.valueOf(bookingId));
        response.put("driverId", String.valueOf(driverId));
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint called by Booking Service to broadcast ride request to drivers.
     * 
     * @param requestDto Contains booking details (bookingId, passenger, locations, status)
     * @return ResponseEntity with success message
     */
    @PostMapping("/newride")
    public ResponseEntity<Map<String, String>> raiseRideRequest(@RequestBody RideRequestDto requestDto) {
        log.info("Received ride request for booking ID: {}", requestDto.getBookingId());
        sendDriversNewRideRequest(requestDto);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Ride request broadcast to drivers");
        response.put("bookingId", String.valueOf(requestDto.getBookingId()));
        return ResponseEntity.ok(response);
    } 

    /**
     * Endpoint called by driver client when driver accepts or rejects a ride.
     * Handles the business logic of forwarding the response to Booking Service.
     * 
     * @param userId The driver ID extracted from the WebSocket destination path
     * @param rideResponseDto Contains response (true/false), bookingId
     */
    @MessageMapping("/rideResponse/{userId}")
    public synchronized void rideResponseHandler(@DestinationVariable String userId, RideResponseDto rideResponseDto) {
        log.info("Received ride response from driver {}. Booking ID: {}, Response: {}", 
                userId,
                rideResponseDto.getBookingId(),
                rideResponseDto.getResponse() ? "ACCEPTED" : "REJECTED");
        
        if (rideResponseDto.getResponse()) {
            // Driver accepted - publish event to Kafka
            Long driverId = Long.parseLong(userId);
            publishRideAcceptedEvent(rideResponseDto.getBookingId(), driverId);
        } else {
            // Driver rejected - just log it (or implement rejection logic)
            log.info("Driver {} rejected booking {}", userId, rideResponseDto.getBookingId());
        }
        
        // Broadcast response to subscribers (for real-time updates)
        simpMessagingTemplate.convertAndSend("/topic/rideResponse/" + rideResponseDto.getBookingId(), rideResponseDto);
        
        log.info("Ride response processing completed for booking ID: {}", rideResponseDto.getBookingId());
    }
    
    /**
     * Publishes ride acceptance event to Kafka.
     * Booking Service will consume this event and update the database.
     * 
     * @param bookingId The booking ID
     * @param driverId The driver ID who accepted the ride
     */
    private void publishRideAcceptedEvent(Long bookingId, Long driverId) {
        String message = String.format("{\"bookingId\":%d,\"driverId\":%d}", bookingId, driverId);
        log.info("Publishing ride accepted event to Kafka. Booking ID: {}, Driver ID: {}", bookingId, driverId);
        kafkaProducerService.publishMessage("ride-accepted", message);
        log.info("Ride accepted event published successfully");
    }
    
    /**
     * OLD METHOD - Replaced by Kafka event-driven approach
     * Previously used to call Booking Service directly via HTTP.
     * Now we publish to Kafka instead for better decoupling.
     */
    /*
    private void updateBookingWithDriver(Long bookingId, Long driverId) {
        try {
            String url = bookingServiceUrl + "/api/v1/bookings/" + bookingId;
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("status", "SCHEDULED");
            requestBody.put("driverId", Optional.of(driverId));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            log.info("Calling Booking Service to update booking. URL: {}, Booking ID: {}, Driver ID: {}", 
                    url, bookingId, driverId);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.PATCH,
                request,
                String.class
            );
            
            log.info("Booking Service update successful. Status: {}, Booking ID: {}", 
                    response.getStatusCode(), bookingId);
            
        } catch (Exception e) {
            log.error("Failed to update Booking Service for booking ID: {}. Error: {}", 
                    bookingId, e.getMessage(), e);
        }
    }
    */

    /**
     * Broadcasts ride request to all connected drivers via WebSocket.
     * Ideally, this should only send to nearby drivers, but for simplicity
     * we broadcast to everyone subscribed to /topic/rideRequest.
     * 
     * @param requestDto The ride request details to broadcast
     */
    public void sendDriversNewRideRequest(RideRequestDto requestDto) {
        log.info("Broadcasting ride request to drivers. Booking ID: {}, Passenger ID: {}, Pickup: ({}, {}), Dropoff: ({}, {})",
                requestDto.getBookingId(),
                requestDto.getPassengerId(),
                requestDto.getStartLatitude(), 
                requestDto.getStartLongitude(),
                requestDto.getEndLatitude(), 
                requestDto.getEndLongitude());
        
        /*
        TODO: 
        Ideally the request should only to nearby drivers, but for simplicity (since we are going to build a basic frontend)
        we send it to everyone
        */
        simpMessagingTemplate.convertAndSend("/topic/rideRequest", requestDto);
        log.info("Ride request broadcast completed for booking ID: {}", requestDto.getBookingId());
    }
}
