package com.girikgarg.uberapigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 🚀 Uber API Gateway - Single Entry Point for All Microservices
 * 
 * This service is the HEART of the microservices architecture, acting as:
 * 
 * 1. 🔐 AUTHENTICATION GATEWAY
 *    - Validates JWT tokens for all protected routes
 *    - Centralized security - backend services trust the gateway
 *    - Public routes (signup/signin) bypass JWT validation
 * 
 * 2. 🌐 SERVICE DISCOVERY CLIENT
 *    - Connects to Eureka Server to discover all backend services
 *    - Dynamic service resolution via lb:// protocol
 *    - No hardcoded IPs - fully dynamic microservices discovery
 * 
 * 3. ⚖️ LOAD BALANCER
 *    - Client-side load balancing using Spring Cloud LoadBalancer
 *    - Distributes requests across multiple service instances
 *    - Automatic failover if a service instance goes down
 * 
 * 4. 🛣️ INTELLIGENT ROUTER
 *    - Routes requests to appropriate microservices based on path
 *    - Path rewriting for clean API URLs
 *    - WebSocket support for real-time communication
 * 
 * 5. 🌍 CORS HANDLER
 *    - Handles cross-origin requests from frontend applications
 *    - Configurable allowed origins, methods, headers
 * 
 * ARCHITECTURE FLOW:
 * ==================
 * Client → API Gateway → Eureka (service discovery) → Backend Service
 *            ↓ (JWT validation)
 *         Auth Service
 * 
 * DEMO HIGHLIGHTS:
 * ===============
 * ✅ Shows centralized authentication at gateway level
 * ✅ Demonstrates service discovery with Eureka
 * ✅ Client-side load balancing in action
 * ✅ Single public endpoint (port 9000) for all services
 * ✅ Backend services in "private network" (only accessible via gateway)
 * 
 * @author Uber Platform Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
public class UberApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(UberApiGatewayApplication.class, args);
        System.out.println("Uber API Gateway started successfully on port 9001");
    }
}
