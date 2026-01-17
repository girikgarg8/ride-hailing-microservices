package com.girikgarg.uberapigateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;

@Configuration
@Slf4j
public class GatewayConfig {

    /**
     * WebClient Builder with Load Balancing
     * 
     * This WebClient is used by AuthenticationDelegationFilter to call Auth Service.
     * The @LoadBalanced annotation enables client-side load balancing via Eureka.
     * 
     * Gateway uses this to delegate authentication decisions to Auth Service.
     * Gateway doesn't know about authentication mechanisms - it just forwards requests.
     */
    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        log.info("✅ WebClient Builder initialized with load balancing");
        return WebClient.builder();
    }
}
