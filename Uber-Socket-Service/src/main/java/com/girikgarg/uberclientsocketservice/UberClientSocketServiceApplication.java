package com.girikgarg.uberclientsocketservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Socket Service for real-time WebSocket communication.
 * Lightweight service with no database dependencies.
 * Registers with Eureka for service discovery.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class UberClientSocketServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UberClientSocketServiceApplication.class, args);
	}

}
