# Demo Entity Consumer Service

A reference implementation service that demonstrates how to properly integrate and use the Uber Entity Service in other microservices.

## Purpose

This service serves as a reference implementation for other microservices, demonstrating:
- ✅ How to import and configure the Entity Service as a dependency
- ✅ Proper entity scanning configuration for shared entities
- ✅ Creating JPA repositories with imported entities
- ✅ Building REST APIs that utilize shared entities
- ✅ Best practices for consuming shared library microservices

## Test Results

### ✅ Build Status: SUCCESS
```bash
./gradlew clean build
# BUILD SUCCESSFUL
```

### ✅ Import Status: SUCCESS
```gradle
dependencies {
    implementation 'com.girikgarg:uber-entity-service:0.0.1-SNAPSHOT'
}
```

### ✅ Runtime Status: SUCCESS
- Service starts on port 8888
- Successfully scans imported entities
- Creates JPA repositories for imported entities
- REST APIs work with imported entities

## Test APIs

### 1. Health Check
```bash
curl http://localhost:8888/api/demo/test
```

**Response:**
```json
{
    "status": "SUCCESS",
    "message": "Demo service is working! ✅",
    "entityServiceImported": "true"
}
```

### 2. Create Driver (Using Imported Entity)
```bash
curl -X POST http://localhost:8888/api/demo/driver \
  -H "Content-Type: application/json" \
  -d '{"name": "John Doe", "licenseNumber": "DL12345", "phoneNumber": "+1234567890"}'
```

**Response:**
```json
{
    "driver": {
        "id": 1,
        "name": "John Doe",
        "licenseNumber": "DL12345",
        "phoneNumber": "+1234567890"
    },
    "message": "Successfully created driver using imported Driver entity! ✅"
}
```

### 3. Create Passenger (Using Imported Entity)
```bash
curl -X POST http://localhost:8888/api/demo/passenger \
  -H "Content-Type: application/json" \
  -d '{"name": "Jane Smith", "email": "jane@example.com", "phoneNumber": "+9876543210", "password": "password123"}'
```

### 4. Get All Drivers
```bash
curl http://localhost:8888/api/demo/drivers
```

### 5. Get All Passengers
```bash
curl http://localhost:8888/api/demo/passengers
```

### 6. Get All Bookings
```bash
curl http://localhost:8888/api/demo/bookings
```

## Running the Demo

```bash
# 1. Make sure Entity Service is published to Maven Local
cd ../Uber-Entity-Service
./gradlew publishToMavenLocal

# 2. Build and run the demo service
cd ../Demo-Entity-Consumer-Service
./gradlew bootRun
```

## Key Learnings

### 1. Maven Local Configuration
```gradle
repositories {
    mavenLocal()  // ← Required to use locally published artifacts
    mavenCentral()
}
```

### 2. Entity Scanning
```java
@EntityScan(basePackages = "com.girikgarg.uberentityservice.models")
```
This tells Spring to scan for entities in the imported package.

### 3. Using Imported Entities
```java
import com.girikgarg.uberentityservice.models.Driver;
import com.girikgarg.uberentityservice.models.Passenger;
import com.girikgarg.uberentityservice.models.Booking;

// Use them like local entities
Driver driver = Driver.builder()
    .name("John")
    .licenseNumber("DL123")
    .build();
```

## Conclusion

This service demonstrates the shared entity library pattern and serves as a reference for implementing other microservices in the platform.

Other microservices (Booking Service, Auth Service, Location Service) can follow this implementation pattern to:
1. Add Entity Service as a dependency
2. Configure entity scanning properly
3. Create JPA repositories with imported entities
4. Build REST APIs using shared data models

## Value for Development Team

- **Learning Resource:** New developers can reference this service to understand entity integration
- **Testing Tool:** Useful for testing Entity Service changes before deploying to production services
- **Documentation:** Live code examples of best practices
- **Template:** Can be used as a starting point for new microservices

