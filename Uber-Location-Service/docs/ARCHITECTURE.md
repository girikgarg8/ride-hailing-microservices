# Ride-Hailing Platform Architecture

## Overview
This document describes the microservices architecture for the real-time ride-hailing platform, focusing on the integration between Location Service, Booking Service, and Client Socket Service for driver-passenger communication.

---

## System Architecture Diagram

```
┌─────────┐                                        ┌─────────────────────────┐
│   D1    │──┐                     ┌──Consumer────▶│   Booking Service       │
└─────────┘  │                     │               │                         │
             │                     │               │  - Receives booking     │
┌─────────┐  │                ┌────┴────┐          │    requests from Queue  │
│   D2    │──┼───WebSocket───▶│  Queue  │          │  - Makes sync call to   │
└─────────┘  │                └────▲────┘          │    fetch nearby drivers │
             │                     │               │                         │
┌─────────┐  │                     │               └────────┬────────────────┘
│   D3    │──┘                     │                        │
└─────────┘                        │                        │
   Drivers                    Producer                      │
                                   │                        │
                              ┌────┴─────────────┐          │
                              │  Client Socket   │          │
                              │    Service       │◀─────────┘
                              │                  │   Async Comm
                              │  - WebSocket hub │
                              │  - Kafka producer│          │
                              │  - Kafka consumer│          │
                              └────┬─────────────┘          │
                                   │                        │
                                   │                        │
                              Consumer                      │
                                   │                        │
                              ┌────▼────┐                   │
                              │  Queue  │                   │
                              └────▲────┘                   │
                                   │                        │
                                   │                        │
                              WebSocket              Sync Comm to fetch
                                   │                 nearby drivers
                                   │                        │
                              ┌────┴────┐                   │
                              │   P1    │                   ▼
                              └─────────┘         ┌─────────────────────┐
                               Passenger          │  Location Service   │
                                                  │                     │
                                                  │  - Redis-based      │
                                                  │  - Geospatial ops   │
                                                  │  - Driver locations │
                                                  └─────────────────────┘
                                                           ▲
                                                           │
                                                      Produce (async)
                                                           │
                                                  Client Socket Service
```

---

## Component Descriptions

### 1. **Client Socket Service** 🔴
**Role**: Real-time communication hub using WebSocket + Kafka

**Responsibilities**:
- Maintains WebSocket connections with all drivers (D1, D2, D3) and passengers (P1)
- Acts as **Kafka Producer**: Publishes location updates to Location Service queue
- Acts as **Kafka Consumer**: Consumes booking requests and delivers to drivers via WebSocket
- Handles bidirectional real-time messaging between drivers and passengers
- Routes private messages to specific users (driver-passenger chat)

**Technology Stack**:
- Spring WebSocket + STOMP
- SockJS (fallback support)
- Kafka integration (producer + consumer)

**Endpoints**:
- `/ws` - WebSocket connection endpoint
- `/app/location` - Driver location updates
- `/topic/booking` - Booking notifications broadcast
- `/user/queue/privateMessage` - Private driver-passenger messages

---

### 2. **Location Service** 🔵
**Role**: Geospatial data storage and retrieval

**Responsibilities**:
- Stores real-time driver locations in Redis using geospatial data structures
- Provides fast geospatial queries to find nearby drivers
- Consumes location updates from Kafka queue (published by Client Socket Service)
- Exposes REST API for synchronous location queries

**Technology Stack**:
- Spring Boot
- Redis (Geospatial commands: `GEOADD`, `GEORADIUS`)
- Jedis client
- Kafka consumer

**API Endpoints**:
- `POST /api/location/drivers` - Save driver location
- `POST /api/location/nearby/drivers` - Get nearby drivers within radius

**Data Flow**:
1. Driver sends location → Client Socket Service
2. Client Socket Service → Kafka Queue (async)
3. Location Service consumes from Queue → Redis

---

### 3. **Booking Service** 🔵
**Role**: Booking orchestration and driver assignment

**Responsibilities**:
- Receives booking requests from passengers (via Client Socket Service queue)
- **Sync call** to Location Service to fetch nearby available drivers
- Selects optimal driver based on distance, rating, availability
- Publishes booking request to Kafka queue
- Client Socket Service consumes and sends to selected drivers via WebSocket

**Technology Stack**:
- Spring Boot
- Kafka producer + consumer
- PostgreSQL (booking persistence)
- REST client (for Location Service communication)

**API Flow**:
1. Passenger requests ride → Client Socket Service → Queue
2. Booking Service consumes from Queue
3. **Sync REST call** to Location Service → Get nearby drivers
4. Select best driver → Produce booking request to Kafka
5. Client Socket Service consumes → Send to driver via WebSocket

---

## Communication Patterns

### **Asynchronous Communication (Kafka Queues)**
- ✅ **Location Updates**: Drivers → Client Socket → Queue → Location Service
- ✅ **Booking Requests**: Booking Service → Queue → Client Socket → Drivers
- ✅ **Decouples services**: Services don't wait for each other
- ✅ **Scalable**: Queue handles high throughput

### **Synchronous Communication (REST API)**
- ✅ **Fetch Nearby Drivers**: Booking Service → Location Service
- ✅ **Immediate response required**: Need driver list before assigning ride
- ✅ **Low latency**: Redis provides sub-millisecond response

### **Real-Time Communication (WebSocket)**
- ✅ **Driver-Server**: Persistent connection for location updates and booking notifications
- ✅ **Passenger-Server**: Ride status updates, driver ETA, chat
- ✅ **Bidirectional**: Both client and server can initiate messages

---

## Data Flow Scenarios

### Scenario 1: Driver Location Update
```
1. Driver (D2) sends location (lat, lng) via WebSocket
2. Client Socket Service receives update
3. Client Socket Service produces to Kafka → "location-updates" topic
4. Location Service consumes from Kafka
5. Location Service stores in Redis: GEOADD drivers:locations lng lat D2
```

### Scenario 2: Passenger Requests Ride
```
1. Passenger (P1) sends ride request via WebSocket
2. Client Socket Service produces to Kafka → "booking-requests" topic
3. Booking Service consumes booking request
4. Booking Service makes sync REST call to Location Service:
   POST /api/location/nearby/drivers { lat, lng }
5. Location Service queries Redis: GEORADIUS drivers:locations lat lng 5km
6. Returns list of nearby drivers [D1, D2, D3]
7. Booking Service selects best driver (D2) based on rating/distance
8. Booking Service produces booking to Kafka → "driver-notifications" topic
9. Client Socket Service consumes notification
10. Client Socket Service sends to D2 via WebSocket: "New booking request!"
```

### Scenario 3: Driver Accepts Ride
```
1. Driver (D2) accepts ride via WebSocket
2. Client Socket Service updates booking status
3. Client Socket Service sends confirmation to Passenger (P1) via WebSocket
4. Booking Service persists booking in PostgreSQL
```

---

## Advantages of This Architecture

### 1. **Scalability**
- Client Socket Service can scale horizontally (multiple instances)
- Kafka ensures load distribution across consumers
- Redis provides fast geospatial lookups even with millions of drivers

### 2. **Decoupling**
- Services communicate via Kafka queues (async)
- No direct service-to-service dependencies (except sync Location Service query)
- Easy to add/remove services without breaking the system

### 3. **Real-Time Performance**
- WebSocket provides sub-100ms latency for driver-passenger communication
- Redis geospatial queries return results in <10ms
- Kafka handles 100k+ messages/sec throughput

### 4. **Fault Tolerance**
- If Location Service is down, updates queue in Kafka
- If Client Socket Service crashes, Kafka retains undelivered messages
- WebSocket reconnection logic handles network disruptions

### 5. **Separation of Concerns**
- **Client Socket Service**: Real-time communication layer
- **Location Service**: Geospatial data store
- **Booking Service**: Business logic and orchestration

---

## Technology Stack Summary

| Component | Technologies |
|-----------|-------------|
| **Client Socket Service** | Spring WebSocket, STOMP, SockJS, Kafka |
| **Location Service** | Spring Boot, Redis, Jedis, Kafka Consumer |
| **Booking Service** | Spring Boot, PostgreSQL, Kafka, REST Client |
| **Message Queue** | Apache Kafka |
| **Cache/Geospatial DB** | Redis |
| **Persistent DB** | PostgreSQL (JPA + Hibernate) |

---

## Future Enhancements

1. **Add Kafka Streams** for real-time analytics (popular routes, driver utilization)
2. **Add API Gateway** (Spring Cloud Gateway) for unified entry point
3. **Add Service Discovery** (Eureka) for dynamic service registration
4. **Add Circuit Breaker** (Resilience4j) for fault tolerance
5. **Add Distributed Tracing** (Sleuth + Zipkin) for request tracking
6. **Add Caching Layer** (Spring Cache + Redis) for frequently accessed data
7. **Add Rate Limiting** to prevent abuse of Location Service API
8. **Add MongoDB** for storing ride history and analytics

---

## Notes

- This architecture prioritizes **real-time performance** and **scalability**
- The hybrid approach (async Kafka + sync REST + real-time WebSocket) balances consistency and performance
- Redis geospatial queries are optimized for ride-hailing use cases (finding nearby drivers)
- Kafka ensures message delivery even during service outages

---

**Last Updated**: January 2026  
**Status**: Implementation in progress


