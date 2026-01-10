package com.girikgarg.ubersocketservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka Configuration for topic management.
 * Automatically creates required Kafka topics on startup.
 */
@Configuration
@EnableKafka
public class KafkaConfig {

    /**
     * Create 'sample' topic if it doesn't exist.     * 
     * @return NewTopic configuration
     */
    @Bean
    public NewTopic sampleTopic() {
        return TopicBuilder.name("sample")
                .partitions(1)
                .replicas(1)
                .build();
    }
    
    /**
     * Create 'ride-accepted' topic for ride acceptance events.
     * When a driver accepts a ride, Socket Service publishes to this topic
     * and Booking Service consumes it to update the booking.
     * 
     * @return NewTopic configuration
     */
    @Bean
    public NewTopic rideAcceptedTopic() {
        return TopicBuilder.name("ride-accepted")
                .partitions(1)
                .replicas(1)
                .build();
    }
}

