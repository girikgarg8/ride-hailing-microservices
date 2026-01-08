package com.girikgarg.uberlocationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class UberLocationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UberLocationServiceApplication.class, args);
	}

}
