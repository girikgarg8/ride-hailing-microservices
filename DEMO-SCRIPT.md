# 🎬 Uber Microservices Platform - Demo Script

**Duration**: 15-20 minutes  
**Audience**: Technical interviewers, recruiters, engineering teams  
**Goal**: Showcase production-grade microservices architecture with real-time features

---

## 🎯 **INTRODUCTION (2 minutes)**

### **Opening Statement**

**Script:**
```
"Hi, I'm Girik. Today I'll demonstrate my Microservices based ride hailing platform


I'll walk you through the complete end-to-end flow using the high-level design diagram, showing how requests flow through the system, how services discover and communicate with each other, and how real-time events are processed.

Let's start with the architecture overview."
```

---

## 🏗️ **ARCHITECTURE WALKTHROUGH (3 minutes)**

### **2.1 High-Level Architecture**

**[Show: High_Level_Design.png]**

**Script:**
```
"Looking at this architecture diagram, let me walk you through the complete request flow and technology stack.

**Request Flow:**
Starting with the client request - all client requests hit our Spring Cloud Gateway, which acts as the single entry point. The Gateway implements authentication delegation by forwarding JWT validation requests to the Auth Service.

**Service Discovery Layer:**
All services register with Netflix Eureka for dynamic service discovery. Services resolve dependencies using service names through the Eureka registry.

**Core Business Services:**
The backend consists of 5 microservices built with Spring Boot and Java 17. The Auth Service handles JWT validation and user management. The Booking Service processes ride requests and contains the core business logic. The Location Service tracks driver positions using Redis geospatial queries for efficient nearby driver searches. The Socket Service manages WebSocket connections to provide real-time notifications to drivers. Finally, the Review Service handles the post-ride feedback system.

**Event-Driven Communication:**
The Kafka messaging layer enables event-driven communication. When ride events occur, services publish messages to Kafka topics, allowing asynchronous processing between the Socket Service and Booking Service.

**Data Layer:**
We have MySQL with Flyway migrations for persistent data, and Redis for geospatial operations that powers our nearby driver search.

**AWS Infrastructure:**
Everything runs on AWS - EC2 instances in a VPC with public/private subnets, managed RDS for MySQL, ElastiCache for Redis, and MSK for Kafka. Backend services are completely isolated in private subnets.

Now let's see the live. demo.."
```


## 🚀 **LIVE INFRASTRUCTURE DEMO (2 minutes)**

### **3.1 AWS Infrastructure**

**[Navigate to: AWS Console > VPC]**

**Script:**
```
"Let me show you the live AWS infrastructure.

Here's our VPC with public and private subnets across two availability zones:
- Public subnets: API Gateway and Eureka (need internet access)
- Private subnets: All backend services (Auth, Booking, Location, Socket, Review)
- NAT Gateway: Provides outbound internet access for private services

[Navigate to: EC2 > Instances]

Here are our EC2 instances:
- Gateway instance: Public subnet, hosts API Gateway
- Eureka instance: Public subnet, service discovery server
- Websocket Client instance: Public subnet, hosts Driver WebSocket Client
- Backend instance: Private subnet, runs all 5 backend services (Auth, Booking, Location, Socket, Review)

Notice the backend instance has NO public IP - completely isolated.

[Navigate to: RDS]

Our MySQL database for persistent data...

[Navigate to: ElastiCache]

Redis cluster for geospatial driver location queries...

[Navigate to: MSK]

And Kafka cluster for event streaming.

All provisioned with Infrastructure as Code.
```

### **3.2 Service Discovery**

**[Navigate to: Eureka Dashboard - http://{{eureka_public_ip}}:8761]**

**Script:**
```
"Here's our  Eureka service registry showing all registered services:

- UBER-API-GATEWAY: 1 instance
- UBER-AUTH-SERVICE: 1 instance  
- UBER-BOOKING-SERVICE: 1 instance
- UBER-LOCATION-SERVICE: 1 instance
- UBER-SOCKET-SERVICE: 1 instance
- UBER-REVIEW-SERVICE: 1 instance

This demonstrates dynamic service discovery where services locate each other through the Eureka registry rather than hardcoded endpoints.

Now let's test the authentication flow..."
```

---

## 🔐 **AUTHENTICATION DELEGATION DEMO (4 minutes)**

### **4.1 Setup CloudWatch Logs**

**[Open: AWS Console > CloudWatch > Log Groups in separate tabs]**

**Script:**
```
"I'm opening CloudWatch log streams for API Gateway and Auth Service 
to show you the authentication delegation pattern in real-time.

This shows the Gateway delegating authentication to the Auth Service for JWT validation"
```

### **4.2 User Registration**

**[Open: Bruno/Postman]**

**Script:**
```
"Let me register a new passenger using our API Gateway:

POST http://{{gateway_public_ip}}:9001/api/v1/auth/signup

[Show request body]
{
  "email": "demo-passenger@uber.com",
  "password": "SecurePass123",
  "name": "Alice Demo",
  "phoneNumber": "+1234567890",
  "role": "PASSENGER"
}

**Curl Command:**
```bash
curl -X POST http://{{gateway_public_ip}}:9001/api/v1/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "demo-passenger@uber.com",
    "password": "SecurePass123",
    "name": "Alice Demo",
    "phoneNumber": "+1234567890",
    "role": "PASSENGER"
  }'
```

[Send request]

Perfect - 201 Created response.

[Point to Gateway logs]
Look at the Gateway logs: "[AUTH] Validating /api/v1/auth/signup with required role: PASSENGER"

[Point to Auth Service logs]  
And Auth Service logs: "Passenger registered successfully"

The Gateway discovered Auth Service via Eureka and routed the request.
```

### **4.3 Authentication & Token Validation**

**Script:**
```
"Now let's sign in and get a JWT token:

POST http://{{gateway_public_ip}}:9001/api/v1/auth/signin

**Curl Command:**
```bash
curl -X POST http://{{gateway_public_ip}}:9001/api/v1/auth/signin \
  -H "Content-Type: application/json" \
  -c cookies.txt \
  -d '{
    "email": "demo-passenger@uber.com", 
    "password": "SecurePass123"
  }'
```

[Show request body]
{
  "email": "demo-passenger@uber.com", 
  "password": "SecurePass123"
}

[Send request, save cookies]

Great - 200 OK with Set-Cookie header containing JWT token.

Now here's the critical part - let me call a protected endpoint:

POST http://{{gateway_public_ip}}:9001/api/v1/auth/validate

**Curl Command:**
```bash
curl -X POST http://{{gateway_public_ip}}:9001/api/v1/auth/validate \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{"requiredRole": "PASSENGER"}'
```

[Point to Gateway logs in real-time]
Watch the Gateway logs:

"[AUTH] Validating /api/v1/auth/validate with required role: PASSENGER"
"[AUTH] Authorized: demo-passenger@uber.com with role PASSENGER -> /api/v1/auth/validate"

[Point to Auth Service logs]
And Auth Service logs:
"INFO AuthController - Validated: email=demo-passenger@uber.com, role=PASSENGER"

The Gateway forwards JWT validation requests to the Auth Service. This implements the authentication delegation pattern, centralizing authentication logic in a dedicated service.
```

---

## 📍 **REDIS GEOSPATIAL DEMO (3 minutes)**

### **5.1 Driver Location Updates**

**Script:**
```
"Now let's test our Redis geospatial features for driver location tracking.

First, I need to register some drivers in the system:

[Register Driver 1]
POST http://{{gateway_public_ip}}:9001/api/v1/auth/signup
{
  "email": "driver1@uber.com",
  "password": "DriverPass123",
  "name": "John Driver",
  "phoneNumber": "+1234567891",
  "role": "DRIVER"
}

[Register Driver 2]  
POST http://{{gateway_public_ip}}:9001/api/v1/auth/signup
{
  "email": "driver2@uber.com",
  "password": "DriverPass123",
  "name": "Jane Driver",
  "phoneNumber": "+1234567892",
  "role": "DRIVER"
}

[Register Driver 3]
POST http://{{gateway_public_ip}}:9001/api/v1/auth/signup
{
  "email": "driver3@uber.com",
  "password": "DriverPass123",
  "name": "Mike Driver",
  "phoneNumber": "+1234567893",
  "role": "DRIVER"
}

**Driver Registration Curl Commands:**
```bash
# Register Driver 1
curl -X POST http://{{gateway_public_ip}}:9001/api/v1/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "driver1@uber.com",
    "password": "DriverPass123",
    "name": "John Driver",
    "phoneNumber": "+1234567891",
    "role": "DRIVER"
  }'

# Register Driver 2
curl -X POST http://{{gateway_public_ip}}:9001/api/v1/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "driver2@uber.com",
    "password": "DriverPass123",
    "name": "Jane Driver",
    "phoneNumber": "+1234567892",
    "role": "DRIVER"
  }'

# Register Driver 3
curl -X POST http://{{gateway_public_ip}}:9001/api/v1/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "driver3@uber.com",
    "password": "DriverPass123",
    "name": "Mike Driver",
    "phoneNumber": "+1234567893",
    "role": "DRIVER"
  }'
```

Great! Now I have three registered drivers. Let me add their locations at different positions in San Francisco:

[Driver 1 - Downtown SF]
POST http://{{gateway_public_ip}}:9001/api/v1/location/drivers
{
  "driverId": 1,
  "latitude": 37.7749,
  "longitude": -122.4194
}

[Driver 2 - Nearby (within 5km)]  
POST http://{{gateway_public_ip}}:9001/api/v1/location/drivers
{
  "driverId": 2,
  "latitude": 37.7850,
  "longitude": -122.4100  
}

[Driver 3 - Far away (outside 5km radius)]
POST http://{{gateway_public_ip}}:9001/api/v1/location/drivers
{
  "driverId": 3,
  "latitude": 37.8200,
  "longitude": -122.3500
}

**Curl Commands:**
```bash
# Driver 1 - Downtown SF
curl -X POST http://{{gateway_public_ip}}:9001/api/v1/location/drivers \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "driverId": 1,
    "latitude": 37.7749,
    "longitude": -122.4194
  }'

# Driver 2 - Nearby (within 5km)
curl -X POST http://{{gateway_public_ip}}:9001/api/v1/location/drivers \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "driverId": 2,
    "latitude": 37.7850,
    "longitude": -122.4100
  }'

# Driver 3 - Far away (outside 5km radius)
curl -X POST http://{{gateway_public_ip}}:9001/api/v1/location/drivers \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "driverId": 3,
    "latitude": 37.8200,
    "longitude": -122.3500
  }'
```

[Point to Location Service logs]
Location Service logs show: "DEBUG RedisLocationServiceImpl - Saved location for driver: 1 at (37.7749, -122.4194) [new]"

Redis is storing these as geospatial coordinates for ultra-fast queries.
```

### **5.2 Nearby Driver Search**

**Script:**
```
"Now let's search for nearby drivers using Redis GEORADIUS:

POST http://{{gateway_public_ip}}:9001/api/v1/location/nearby/drivers
{
  "latitude": 37.7749,
  "longitude": -122.4194
}

**Curl Command:**
```bash
curl -X POST http://{{gateway_public_ip}}:9001/api/v1/location/nearby/drivers \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "latitude": 37.7749,
    "longitude": -122.4194
  }'
```

[Send request]

Response shows:
{
  "drivers": [
    {"id": 1, "distance": 0.0},
    {"id": 2, "distance": 1.2}
  ]
}

Notice driver 3 is NOT returned - it's too far away!

[Point to Location Service logs]
"DEBUG RedisLocationServiceImpl - Found 2 drivers near location (37.7749, -122.4194) within 5.0 km"

This Redis geospatial query executes in milliseconds and can handle 
millions of driver locations. Notice how Driver 3 at (37.8200, -122.3500) 
is automatically excluded - it's approximately 6.2km away, outside our 5km search radius.
```

---

## 🎯 **COMPLETE BOOKING FLOW WITH KAFKA (5 minutes)**

### **6.1 Setup Multi-Service Logs**

**[Open: CloudWatch logs for Gateway, Booking, Location, Socket services]**

**Script:**
```
"I'm opening log streams for four services to show you the complete 
booking flow with event-driven architecture:

- API Gateway (authentication & routing)
- Booking Service (business logic & database)  
- Location Service (nearby driver search)
- Socket Service (WebSocket & Kafka events)

Watch how they orchestrate together..."
```

### **6.2 Real-Time Driver Connection**

**[Open: Driver WebSocket Client - http://{{websocket_client_public_ip}}:3000]**

**Script:**
```
"Now let's see the real-time WebSocket communication before creating a booking.

Here's our Driver Portal running on a dedicated EC2 instance - a full-stack client that authenticates 
via the API Gateway and connects to WebSocket for real-time notifications.

[Login as driver]
Email: demo-driver@uber.com
Password: driver123

[Click "Go Online"]

Perfect! The driver is now connected via WebSocket through the API Gateway and ready to receive ride requests.

Now let's create a booking to trigger a real-time notification to this connected driver.
```

### **6.3 Create Booking with Real-Time Notifications**

**Script:**
```
"Let me create a ride booking that will instantly notify our connected driver:

POST http://{{gateway_public_ip}}:9001/api/v1/bookings
{
  "passengerId": 1,
  "startLocation": {
    "latitude": 37.7749,
    "longitude": -122.4194
  },
  "endLocation": {
    "latitude": 37.7849, 
    "longitude": -122.4094
  }
}

**Curl Command:**
```bash
curl -X POST http://{{gateway_public_ip}}:9001/api/v1/bookings \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "passengerId": 1,
    "startLocation": {
      "latitude": 37.7749,
      "longitude": -122.4194
    },
    "endLocation": {
      "latitude": 37.7849, 
      "longitude": -122.4094
    }
  }'
```

[Send request]

Perfect - booking created with ID 42.

[Point to driver client immediately]
Look! Instantly, the driver receives the ride request with all booking details!

Now watch the logs in sequence:

[Point to Gateway logs - timestamp 10:30:15.000]
"[AUTH] Validating /api/v1/bookings with required role: PASSENGER"
"[AUTH] Authorized: demo-passenger@uber.com with role PASSENGER -> /api/v1/bookings"

[Point to Booking Service logs - timestamp 10:30:15.100]  
"INFO BookingServiceImpl - Creating booking for passenger: 1"
"INFO BookingServiceImpl - Found passenger: 1"
"INFO BookingServiceImpl - Booking created with ID: 42"

[Point to Location Service logs - timestamp 10:30:15.250]
"DEBUG RedisLocationServiceImpl - Found 2 drivers near location (37.7749, -122.4194) within 5.0 km"

[Point to Socket Service logs]
"INFO DriverRequestController - Broadcasting ride request to 2 drivers via WebSocket"
"INFO DriverRequestController - Broadcast completed - 2 drivers notified"

This demonstrates production-grade real-time communication with proper authentication and instant notifications.
```

### **6.4 Event-Driven Ride Acceptance**

**Script:**
```
"Now for the event-driven architecture with Kafka.

[Click "Accept Ride" in driver client]

Watch what happens:

[Point to Socket Service logs]
"INFO DriverRequestController - Driver 1 accepted booking 42"
"INFO KafkaProducerService - Publishing message to Kafka topic 'ride-accepted': {\"bookingId\":42,\"driverId\":1}"
"INFO KafkaProducerService - Message published successfully to topic 'ride-accepted'"

[Point to Booking Service logs - 100ms later]
"INFO KafkaConsumerService - Received ride accepted event from Kafka: {\"bookingId\":42,\"driverId\":1}"
"INFO KafkaConsumerService - Processing ride acceptance. Booking ID: 42, Driver ID: 1"
"INFO KafkaConsumerService - Booking updated successfully. Booking ID: 42, Status: SCHEDULED, Driver: Jane Driver (ID: 1)"

Socket Service and Booking Service are completely decoupled via Kafka, creating an asynchronous and resilient event-driven architecture.

The booking status updated from REQUESTED to SCHEDULED automatically.
```

---


## 🎯 **WRAP-UP**

**Script:**
```
"That concludes the demonstration. 
Thank you so much for watching and have a nice day!"
```

---















## 📋 **DEMO PREPARATION CHECKLIST**

### **Before Recording:**

**Infrastructure Setup:**
- [ ] Deploy all AWS resources using AWS-DEPLOYMENT-GUIDE.md
- [ ] Verify all 7 services registered in Eureka
- [ ] Test authentication flow end-to-end
- [ ] Confirm WebSocket client accessible
- [ ] Validate all CloudWatch log streams active

**Browser Tabs Setup:**
- [ ] AWS Console (VPC, EC2, RDS, ElastiCache, MSK)
- [ ] Eureka Dashboard (http://eureka-ip:8761)
- [ ] CloudWatch Log Groups (Gateway, Auth, Booking, Location, Socket)
- [ ] Driver WebSocket Client (http://{{websocket_client_public_ip}}:3000)
- [ ] Bruno/Postman with pre-configured requests

**Test Data:**
- [ ] Demo passenger account created
- [ ] Demo driver account created  
- [ ] Sample driver locations added to Redis
- [ ] All API requests tested and working

**Backup Plans:**
- [ ] Screenshots of working system (if live demo fails)
- [ ] Pre-recorded video segments for critical flows
- [ ] Alternative demo endpoints ready

---

**Remember**: This demo showcases skills that 99.99% of developers never implement. You're demonstrating enterprise-level backend engineering! 🚀