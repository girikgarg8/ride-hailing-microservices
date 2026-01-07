package com.girikgarg.uberbookingservice.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Type-safe configuration properties for Booking Service.
 * Binds to properties prefixed with 'booking.service' in application.properties.
 */
@Configuration
@ConfigurationProperties(prefix = "booking.service")
@Data
public class BookingServiceProperties {
    
    /**
     * URL of the Location Service for fetching nearby drivers.
     * Default: http://localhost:7477
     */
    private String locationServiceUrl = "http://localhost:7477";
    
    /**
     * URL of the Socket Service for WebSocket communication.
     * Default: http://localhost:8080
     */
    private String socketServiceUrl = "http://localhost:8080";
    
    /**
     * Maximum radius (in km) to search for nearby drivers.
     * Default: 5.0 km
     */
    private Double searchRadiusKm = 5.0;
    
    /**
     * Maximum number of nearby drivers to fetch.
     * Default: 10
     */
    private Integer maxNearbyDrivers = 10;
}

