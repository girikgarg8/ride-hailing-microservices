# Uber Location Service

A Redis-based microservice for real-time location tracking and management in the ride-hailing platform.

## Purpose

The Location Service is responsible for:
- **Real-time location tracking** of drivers and passengers
- **Geospatial queries** (finding nearby drivers)
- **Location caching** for performance optimization
- **Live location updates** via WebSocket/REST APIs

## Tech Stack

- **Spring Boot 3.2.5** - Application framework
- **Spring Data Redis** - Redis integration
- **Redis** - In-memory data store for location data
- **Lombok** - Reducing boilerplate code
- **Java 17** - Programming language

## Prerequisites

- **Java 17+**
- **Gradle 8.7**
- **Redis Server** (running on localhost:6379)

## Configuration

### Application Properties
```properties
server.port=7477
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

## Build & Run

### Build the project
```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/amazon-corretto-17.jdk/Contents/Home
./gradlew clean build
```

### Run the service
```bash
./gradlew bootRun
```

### Check Redis is running
```bash
redis-cli ping
# Expected: PONG
```

## Usage Examples

### Store Driver Location (Example)
```bash
curl -X POST http://localhost:7477/api/locations/driver \
  -H "Content-Type: application/json" \
  -d '{
    "driverId": 123,
    "latitude": 28.7041,
    "longitude": 77.1025
  }'
```

### Find Nearby Drivers (Example)
```bash
curl "http://localhost:7477/api/locations/nearby?lat=28.7041&lon=77.1025&radius=5"
```

## Redis Data Structures

This service leverages Redis for:
- **Geospatial indexes** (GEOADD, GEORADIUS) for location-based queries
- **Hash maps** for storing driver/passenger metadata
- **Pub/Sub** for real-time location updates

## Architecture

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       ▼
┌─────────────────────────┐
│  Location Service       │
│  (Spring Boot)          │
│  Port: 7477             │
└───────────┬─────────────┘
            │
            ▼
    ┌──────────────┐
    │    Redis     │
    │  Port: 6379  │
    └──────────────┘
```

## Development

### Key Directories
- `src/main/java/com/girikgarg/uberlocationservice/` - Java source code
- `src/main/resources/` - Configuration files
- `build.gradle` - Build configuration

### Testing
```bash
./gradlew test
```

## Notes

- This service uses Redis for **high-performance, in-memory** location tracking
- Data is **volatile** - consider persisting critical location history to a database
- Redis Pub/Sub can be used for **real-time location streaming** to connected clients
- For production, configure **Redis persistence** (RDB/AOF) and **replication**

## Status

✅ **Build Status:** Successful  
✅ **Redis Connection:** Connected  
✅ **Service Running:** Port 7477

