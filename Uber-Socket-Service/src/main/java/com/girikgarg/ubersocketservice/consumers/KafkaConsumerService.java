package com.girikgarg.ubersocketservice.consumers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Kafka Consumer Service for consuming messages from Kafka topics.
 */
@Service
@Slf4j
public class KafkaConsumerService {

    /**
     * Consume messages from the sample topic.
     * 
     * @param message The message consumed from Kafka
     */
    @KafkaListener(topics = "sample", groupId = "socket-service-group")
    public void consumeRideEvents(String message) {
        log.info("Kafka message consumed from topic 'sample': {}", message);
        // Process the event here (e.g., analytics, logging, notifications)
    }
}

