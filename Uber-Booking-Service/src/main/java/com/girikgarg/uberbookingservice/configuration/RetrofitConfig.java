package com.girikgarg.uberbookingservice.configuration;

import com.girikgarg.uberbookingservice.apis.LocationServiceApi;
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
 * It supports both:
 * 1. Direct URL approach (using application.properties)
 * 2. Service Discovery approach (using Eureka to find service instances)
 * 
 * Currently using Eureka-based service discovery.
 */
@Slf4j
@Configuration
public class RetrofitConfig {

    private final BookingServiceProperties properties;
    
    @Autowired(required = false)
    private EurekaClient eurekaClient;

    public RetrofitConfig(BookingServiceProperties properties) {
        this.properties = properties;
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
     * Falls back to hardcoded URL from application.properties if Eureka is unavailable.
     * 
     * Service Discovery Flow:
     * 1. Query Eureka Server for "UBER-LOCATION-SERVICE" instances
     * 2. Get next available server (with load balancing)
     * 3. Use the home page URL of that instance
     * 4. If Eureka unavailable, use fallback URL
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
     * 
     * @param serviceName The name of the service as registered in Eureka
     * @return The home page URL of the service instance
     */
    private String getServiceUrl(String serviceName) {
        if (eurekaClient != null) {
            try {
                log.info("📡 Querying Eureka Server for service: {}", serviceName);
                String homePageUrl = eurekaClient.getNextServerFromEureka(serviceName, false).getHomePageUrl();
                log.info("📍 Successfully retrieved from Eureka: {} -> {}", serviceName, homePageUrl);
                return homePageUrl;
            } catch (Exception e) {
                log.warn("⚠️ Failed to get service from Eureka: {}", e.getMessage());
                log.warn("📌 Falling back to hardcoded URL from application.properties");
            }
        } else {
            log.warn("⚠️ EurekaClient is not available, using hardcoded URL");
        }
        String fallbackUrl = properties.getLocationServiceUrl();
        log.info("Using fallback URL: {}", fallbackUrl);
        return fallbackUrl;
    }

    /**
     * Create LocationServiceApi bean using Retrofit.
     * This bean can be injected into services for making API calls.
     */
    @Bean
    public LocationServiceApi locationServiceApi(Retrofit locationServiceRetrofit) {
        return locationServiceRetrofit.create(LocationServiceApi.class);
    }
}

