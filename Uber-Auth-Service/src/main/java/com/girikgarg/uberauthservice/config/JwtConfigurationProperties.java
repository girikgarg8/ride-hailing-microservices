package com.girikgarg.uberauthservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * JWT Configuration Properties
 * Binds to 'jwt.*' properties in application.properties
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
@Data
@Validated
public class JwtConfigurationProperties {
    
    /**
     * JWT expiry time in seconds
     * Default: 3600 seconds (1 hour)
     */
    @Min(value = 60, message = "JWT expiry must be at least 60 seconds")
    private int expiry = 3600;
    
    /**
     * JWT secret key for signing tokens
     * Minimum length: 32 characters for HS256 (256 bits)
     */
    @NotBlank(message = "JWT secret cannot be blank")
    @Size(min = 32, message = "JWT secret must be at least 32 characters for HS256")
    private String secret;
}
