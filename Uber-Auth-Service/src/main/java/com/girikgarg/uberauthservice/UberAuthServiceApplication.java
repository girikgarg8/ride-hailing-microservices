package com.girikgarg.uberauthservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaAuditing
@EntityScan(basePackages = {"com.girikgarg.uberentityservice.models"})
public class UberAuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UberAuthServiceApplication.class, args);
    }
}
