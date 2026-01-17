# 🚀 Uber API Gateway

## Overview

The **Uber API Gateway** is the **single entry point** for all client requests in the Uber microservices platform. It acts as a reverse proxy that routes requests to appropriate backend services while providing centralized authentication, load balancing, and service discovery.

---

## 🎯 Key Responsibilities

### 1. **🔐 Centralized Authentication**
- Validates JWT tokens for all protected routes
- Backend services trust the gateway (no need for JWT validation in each service)
- Public routes (signup/signin) bypass authentication

### 2. **🌐 Service Discovery Integration**
- Connects to Eureka Server to discover backend services dynamically
- No hardcoded service URLs - fully dynamic service resolution
- Uses `lb://SERVICE-NAME` protocol for load-balanced routing

### 3. **⚖️ Client-Side Load Balancing**
- Distributes requests across multiple service instances
- Automatic failover if a service instance goes down
- Uses Spring Cloud LoadBalancer

### 4. **🛣️ Intelligent Routing**
- Routes requests to appropriate microservices based on path
- Path rewriting for clean API URLs
- WebSocket support for real-time communication

### 5. **🌍 CORS Handling**
- Handles cross-origin requests from frontend applications
- Configurable allowed origins, methods, headers

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT (Browser/Mobile)                   │
└───────────────────────────────┬─────────────────────────────────┘
                                │
                                │ HTTP/WebSocket Requests
                                │
                ┌───────────────▼────────────────┐
                │                                 │
                │      🚀 API GATEWAY (9000)     │
                │                                 │
                │  ┌──────────────────────────┐  │
                │  │  JWT Authentication      │  │
                │  │  Filter                  │  │
                │  └────────────┬─────────────┘  │
                │               │                 │
                │  ┌────────────▼─────────────┐  │
                │  │  Service Discovery       │  │
                │  │  (Eureka Client)         │  │
                │  └────────────┬─────────────┘  │
                │               │                 │
                │  ┌────────────▼─────────────┐  │
                │  │  Load Balancer           │  │
                │  └────────────┬─────────────┘  │
                │               │                 │
                └───────────────┼─────────────────┘
                                │
        ┌───────────────────────┼───────────────────────┐
        │                       │                       │
┌───────▼────────┐  ┌──────────▼────────┐  ┌──────────▼────────┐
│  Auth Service  │  │  Booking Service  │  │  Location Service │
│    (9090)      │  │     (8001)        │  │     (7070)        │
└────────────────┘  └───────────────────┘  └───────────────────┘
        │                       │                       │
        └───────────────────────┼───────────────────────┘
                                │
                    ┌───────────▼────────────┐
                    │   Eureka Server (8761) │
                    └────────────────────────┘
```

---

## 📋 Prerequisites

- Java 17 or higher
- Gradle 7.x or higher
- Eureka Server running on port 8761
- Backend services (Auth, Booking, Location, etc.) running

---

## 🚀 Quick Start

### 1. Build the Gateway

```bash
cd Uber-API-Gateway
./gradlew clean build
```

### 2. Run the Gateway

```bash
./gradlew bootRun
```

Or run the JAR:

```bash
java -jar build/libs/Uber-API-Gateway-0.0.1-SNAPSHOT.jar
```

### 3. Verify Gateway is Running

```bash
# Health check
curl http://localhost:9000/actuator/health

# View all routes
curl http://localhost:9000/actuator/gateway/routes | jq
```

---

## 🛣️ API Routes

### **Public Routes (No JWT Required)**

| Method | Path | Description | Backend Service |
|--------|------|-------------|-----------------|
| POST | `/api/v1/auth/signup/passenger` | Register new passenger | Auth Service |
| POST | `/api/v1/auth/signin/passenger` | Login and get JWT token | Auth Service |

### **Protected Routes (JWT Required)**

| Method | Path | Description | Backend Service |
|--------|------|-------------|-----------------|
| GET | `/api/v1/auth/validate` | Validate JWT token | Auth Service |
| POST | `/api/v1/bookings` | Create new booking | Booking Service |
| GET | `/api/v1/bookings` | Get all bookings | Booking Service |
| GET | `/api/v1/bookings/{id}` | Get booking by ID | Booking Service |
| POST | `/api/v1/location/drivers/{id}` | Update driver location | Location Service |
| GET | `/api/v1/location/nearby` | Find nearby drivers | Location Service |
| POST | `/api/v1/reviews` | Create review | Review Service |
| GET | `/api/v1/reviews/booking/{bookingId}` | Get reviews for booking | Review Service |

### **WebSocket Routes**

| Path | Description | Backend Service |
|------|-------------|-----------------|
| `/ws/**` | WebSocket connections for drivers | Socket Service |

---

## 🔐 Authentication Flow

### 1. **User Signs Up/Signs In**

```bash
# Sign up
curl -X POST http://localhost:9000/api/v1/auth/signup/passenger \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123",
    "name": "John Doe",
    "phoneNumber": "+1234567890"
  }'

# Sign in (get JWT token in cookie)
curl -c cookies.txt -X POST http://localhost:9000/api/v1/auth/signin/passenger \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

### 2. **Use JWT Token for Protected Routes**

```bash
# Option 1: Using cookie (automatic if browser)
curl -b cookies.txt http://localhost:9000/api/v1/auth/validate

# Option 2: Using Authorization header
TOKEN="<jwt-token-here>"
curl -H "Authorization: Bearer $TOKEN" \
     http://localhost:9000/api/v1/bookings
```

---

## 🧪 Testing

See [TESTING-GUIDE.md](../TESTING-GUIDE.md) for comprehensive testing scenarios.

Quick test:

```bash
# 1. Start all services (Eureka, Auth, Booking, Location, Socket, Gateway)
# 2. Run this test script:

# Sign up
curl -X POST http://localhost:9000/api/v1/auth/signup/passenger \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"pass123","name":"Test","phoneNumber":"1234567890"}'

# Sign in
curl -c /tmp/cookies.txt -X POST http://localhost:9000/api/v1/auth/signin/passenger \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"pass123"}'

# Test protected route
curl -b /tmp/cookies.txt http://localhost:9000/api/v1/auth/validate
```

---

## ⚙️ Configuration

Key configuration in `application.yml`:

```yaml
server:
  port: 9000  # Gateway port

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/  # Eureka server

jwt:
  secret: <must-match-auth-service>  # JWT secret for validation
  expiry: 3600  # Token expiry in seconds
```

---

## 🎥 Demo Points

When demonstrating this gateway, highlight:

1. **Centralized Authentication**: Show how invalid tokens are rejected at gateway level
2. **Service Discovery**: Show Eureka dashboard with all services registered
3. **Load Balancing**: Start multiple instances of a service, show requests distributed
4. **Intelligent Routing**: Show how different paths route to different services
5. **Security**: Backend services are not directly accessible (only through gateway)

---

## 📊 Monitoring

### Health Check
```bash
curl http://localhost:9000/actuator/health
```

### View All Routes
```bash
curl http://localhost:9000/actuator/gateway/routes | jq
```

### Metrics
```bash
curl http://localhost:9000/actuator/metrics
```

---

## 🐛 Troubleshooting

### Gateway can't find services

**Problem**: `503 Service Unavailable`

**Solution**:
1. Check Eureka dashboard: http://localhost:8761
2. Ensure backend services are registered with Eureka
3. Check `eureka.client.service-url.defaultZone` in application.yml

### JWT validation fails

**Problem**: `401 Unauthorized`

**Solution**:
1. Verify JWT secret matches Auth Service
2. Check token is not expired
3. Ensure token is sent in `Authorization: Bearer <token>` or Cookie

### CORS errors

**Problem**: Browser blocks requests

**Solution**:
1. Check CORS configuration in `GatewayConfig.java`
2. Ensure `allowedOriginPatterns` includes your frontend URL
3. Verify `allowCredentials` is set to `true`

---

## 📚 Related Documentation

- [Complete Demo Guide](../DEMO-GUIDE.md)
- [AWS Deployment Guide](../AWS-DEPLOYMENT-GUIDE.md)
- [Testing Guide](../TESTING-GUIDE.md)

---

## 🤝 Contributing

This is part of the Uber Microservices Platform. For questions or improvements, contact the platform team.

---

**Built with ❤️ by Uber Platform Team**
