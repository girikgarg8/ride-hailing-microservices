package com.girikgarg.uberbookingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableDiscoveryClient  // Enable Eureka client for service discovery
@EntityScan(basePackages = {
		"com.girikgarg.uberentityservice.models",  // Scan entities from uber-entity-service
		"com.girikgarg.uberbookingservice"         // Scan local entities if any
})
@EnableJpaRepositories(basePackages = "com.girikgarg.uberbookingservice.repositories")
@EnableJpaAuditing  // Enable JPA auditing for @CreatedDate/@LastModifiedDate
public class UberBookingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UberBookingServiceApplication.class, args);
	}

}
