package com.girikgarg.ubersocketservice.configuration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for RestTemplate to make HTTP calls to other services.
 * Configured to support PATCH method for updating bookings.
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        // Use HttpComponentsClientHttpRequestFactory to support PATCH method
        return builder
                .requestFactory(HttpComponentsClientHttpRequestFactory.class)
                .build();
    }
}

