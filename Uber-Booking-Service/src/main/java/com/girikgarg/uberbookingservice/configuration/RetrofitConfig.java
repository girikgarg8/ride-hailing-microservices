package com.girikgarg.uberbookingservice.configuration;

import com.girikgarg.uberbookingservice.apis.LocationServiceApi;
import com.girikgarg.uberbookingservice.apis.UberSocketApi;
import com.netflix.discovery.EurekaClient;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

/**
 * Configuration class for Retrofit HTTP client.
 * 
 * This configuration creates Retrofit instances for communicating with other microservices.
 * Uses Eureka-based service discovery exclusively (no fallback).
 * 
 * The application will fail fast if:
 * - Eureka Server is unavailable
 * - Required service is not registered in Eureka
 * 
 * This ensures we know immediately when service discovery is not working.
 */
@Slf4j
@Configuration
public class RetrofitConfig {

    @Autowired
    private EurekaClient eurekaClient;

    public RetrofitConfig() {
    }

    /**
     * Create OkHttpClient with custom timeouts.
     * This client is used by Retrofit for making HTTP requests.
     */
    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Create Retrofit instance for Location Service.
     * 
     * Uses EurekaClient to dynamically fetch the service URL from Eureka Server.
     * 
     * Service Discovery Flow:
     * 1. Query Eureka Server for "UBER-LOCATION-SERVICE" instances
     * 2. Get next available server (with load balancing)
     * 3. Use the home page URL of that instance
     * 4. Fails fast if service is not available
     */
    @Bean
    public Retrofit locationServiceRetrofit(OkHttpClient okHttpClient) {
        log.info("🔍 Initializing Retrofit with Eureka Service Discovery...");
        String serviceUrl = getServiceUrl("UBER-LOCATION-SERVICE");
        log.info("✅ Retrofit configured with base URL: {}", serviceUrl);
        
        return new Retrofit.Builder()
                .baseUrl(serviceUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
    }

    /**
     * Get service URL from Eureka Server.
     * No fallback - fails fast if Eureka is unavailable or service not found.
     * 
     * @param serviceName The name of the service as registered in Eureka
     * @return The home page URL of the service instance
     * @throws RuntimeException if Eureka is unavailable or service not found
     */
    private String getServiceUrl(String serviceName) {
        if (eurekaClient == null) {
            log.error("❌ EurekaClient is not available!");
            throw new RuntimeException("EurekaClient is not available - cannot discover service: " + serviceName);
        }
        
        try {
            log.info("📡 Querying Eureka Server for service: {}", serviceName);
            String homePageUrl = eurekaClient.getNextServerFromEureka(serviceName, false).getHomePageUrl();
            log.info("✅ Successfully retrieved from Eureka: {} -> {}", serviceName, homePageUrl);
            return homePageUrl;
        } catch (Exception e) {
            log.error("❌ Failed to discover service '{}' from Eureka: {}", serviceName, e.getMessage());
            throw new RuntimeException("Unable to discover service '" + serviceName + "' from Eureka", e);
        }
    }

    /**
     * Create LocationServiceApi bean using Retrofit.
     * This bean can be injected into services for making API calls.
     */
    @Bean
    public LocationServiceApi locationServiceApi(Retrofit locationServiceRetrofit) {
        return locationServiceRetrofit.create(LocationServiceApi.class);
    }

    /**
     * Create Retrofit instance for Socket Service.
     * 
     * Uses EurekaClient to dynamically fetch the service URL from Eureka Server.
     * Fails fast if service is not available in Eureka.
     */
    @Bean
    public Retrofit socketServiceRetrofit(OkHttpClient okHttpClient) {
        log.info("🔍 Initializing Retrofit for Socket Service with Eureka...");
        String serviceUrl = getServiceUrl("UBER-SOCKET-SERVICE");
        log.info("✅ Socket Service Retrofit configured with base URL: {}", serviceUrl);
        
        return new Retrofit.Builder()
                .baseUrl(serviceUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
    }

    /**
     * Create UberSocketApi bean using Retrofit.
     * This bean can be injected into services for making API calls to Socket Service.
     */
    @Bean
    public UberSocketApi uberSocketApi(Retrofit socketServiceRetrofit) {
        return socketServiceRetrofit.create(UberSocketApi.class);
    }
}

