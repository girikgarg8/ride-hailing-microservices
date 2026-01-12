package com.girikgarg.uberreviewservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Jackson configuration to handle Hibernate lazy loading proxies.
 * 
 * This configuration:
 * - Registers Hibernate6Module to handle Hibernate proxies and lazy-loaded entities
 * - Prevents serialization errors when lazy-loaded entities are accessed
 * - Configures Jackson to ignore uninitialized lazy properties
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.build();
        
        // Register Hibernate module to handle lazy loading
        Hibernate6Module hibernate6Module = new Hibernate6Module();
        
        // Don't serialize lazy-loaded properties that haven't been initialized
        hibernate6Module.configure(Hibernate6Module.Feature.FORCE_LAZY_LOADING, false);
        
        // Serialize identifier for lazy-loaded associations
        hibernate6Module.configure(Hibernate6Module.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS, true);
        
        objectMapper.registerModule(hibernate6Module);
        
        // Disable writing dates as timestamps (use ISO-8601 format)
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        return objectMapper;
    }
}
