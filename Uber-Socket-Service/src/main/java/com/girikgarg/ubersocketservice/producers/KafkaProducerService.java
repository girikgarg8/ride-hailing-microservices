package com.girikgarg.ubersocketservice.producers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Kafka Producer Service for publishing messages to Kafka topics.
 * Used for event-driven communication between microservices.
 */
@Service
@Slf4j
public class KafkaProducerService {
    
    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publish a message to a Kafka topic.
     * 
     * @param topic   The Kafka topic to publish to
     * @param message The message to publish
     */
    public void publishMessage(String topic, String message) {
        log.info("Publishing message to Kafka topic '{}': {}", topic, message);
        kafkaTemplate.send(topic, message);
        log.info("Message published successfully to topic '{}'", topic);
    }
}