# 🚕 Uber Booking Service

The Booking Service is the core orchestrator for ride requests in the Uber microservices platform. It handles booking creation, driver matching through Location Service, and coordinates real-time communication with drivers.

---

## 📋 Features

- ✅ Create ride bookings with start/end locations
- ✅ Validate passenger existence
- ✅ Fetch nearby drivers from Location Service (5km radius)
- ✅ Persistent booking storage with status tracking
- ✅ RESTful API with proper error handling
- ✅ Comprehensive logging
- 🚧 Driver assignment via WebSocket (Future)
- 🚧 Real-time status updates (Future)

---

## 🏗️ Architecture

### Package Structure
```
com.girikgarg.uberbookingservice/
├── configuration/          # Spring configurations (RestTemplate, etc.)
├── controllers/           # REST API endpoints
├── services/
│   ├── api/              # Service interfaces
│   └── impl/             # Service implementations
├── repositories/          # JPA repositories
└── dto/                  # Data Transfer Objects
```

### Dependencies
- **Uber Entity Service**: Shared domain models (Booking, Passenger, Driver)
- **Location Service** (Port 7477): Geospatial queries for nearby drivers
- **Client Socket Service** (Port 8080): WebSocket communication (Future)
- **MySQL**: Persistent storage
- **Spring Data JPA**: Database access
- **RestTemplate**: Synchronous HTTP communication

---

## 🚀 Getting Started

### Prerequisites
1. **Java 17** installed
2. **MySQL** running on localhost:3306
3. **Uber-Entity-Service** published to Maven Local
4. **Location Service** running on port 7477
5. **Redis** running (for Location Service)
6. Database `Uber_Db_Local` created and migrated

### Environment Variables
```bash
# Required: MySQL password
export MYSQL_LOCAL_PASSWORD="your_password"
```

### Build & Run

#### 1. Ensure Entity Service is Published
```bash
cd ../Uber-Entity-Service
./gradlew clean build publishToMavenLocal -x test
```

#### 2. Build Booking Service
```bash
cd Uber-Booking-Service
./gradlew clean build -x test
```

#### 3. Run the Service
```bash
./gradlew bootRun
```

The service will start on **port 7475**.

---

## 📡 API Endpoints

### Create Booking
**POST** `/api/v1/bookings`

**Request:**
```json
{
  "passengerId": 1,
  "startLocation": {
    "latitude": 28.7041,
    "longitude": 77.1025
  },
  "endLocation": {
    "latitude": 28.5355,
    "longitude": 77.3910
  }
}
```

**Response (201 Created):**
```json
{
  "bookingId": 123,
  "bookingStatus": "ASSIGNING_DRIVER",
  "driver": null
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:7475/api/v1/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "passengerId": 1,
    "startLocation": {
      "latitude": 28.7041,
      "longitude": 77.1025
    },
    "endLocation": {
      "latitude": 28.5355,
      "longitude": 77.3910
    }
  }'
```

---

## 🔄 End-to-End Flow

1. **Passenger Request**: Client sends POST request with booking details
2. **Validation**: Service validates passenger exists in database
3. **Booking Creation**: Creates booking with `ASSIGNING_DRIVER` status
4. **Driver Search**: Calls Location Service to get nearby drivers (5km radius)
5. **Logging**: Logs all nearby drivers (Future: WebSocket broadcast)
6. **Response**: Returns booking details to client

**Future Flow:**
- Send ride request to all nearby drivers via WebSocket
- Wait for driver acceptance (first-come-first-served)
- Update booking with assigned driver
- Notify passenger via WebSocket

---

## ⚙️ Configuration

### application.properties
```properties
# Server
server.port=7475

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/Uber_Db_Local
spring.datasource.username=root
spring.datasource.password=${MYSQL_LOCAL_PASSWORD}

# JPA
spring.jpa.show-sql=true
spring.jpa.hibernate.ddl-auto=validate

# Microservices
location.service.url=http://localhost:7477
socket.service.url=http://localhost:8080
```

### RestTemplate Configuration
- **Connect Timeout**: 5 seconds
- **Read Timeout**: 10 seconds
- **Bean**: Configured in `RestTemplateConfig.java`

---

## 🧪 Testing

### Run Test Script
```bash
./test-booking-service.sh
```

### Manual Testing

1. **Start Required Services:**
   ```bash
   # Terminal 1: Location Service
   cd ../Uber-Location-Service && ./gradlew bootRun
   
   # Terminal 2: Booking Service
   cd ../Uber-Booking-Service && ./gradlew bootRun
   ```

2. **Add Test Data:**
   ```sql
   -- Create a passenger
   INSERT INTO passenger (id, created_at, updated_at, phone_number, name) 
   VALUES (1, NOW(), NOW(), '9876543210', 'Test Passenger');
   
   -- Add drivers to Location Service
   curl -X POST http://localhost:7477/api/location/drivers \
     -H "Content-Type: application/json" \
     -d '{"driverId": "D001", "latitude": 28.7050, "longitude": 77.1030}'
   ```

3. **Create Booking:**
   ```bash
   curl -X POST http://localhost:7475/api/v1/bookings \
     -H "Content-Type: application/json" \
     -d '{
       "passengerId": 1,
       "startLocation": {"latitude": 28.7041, "longitude": 77.1025},
       "endLocation": {"latitude": 28.5355, "longitude": 77.3910}
     }'
   ```

---

## 📊 Database Schema

### Tables Used
- **booking**: Stores ride bookings
- **passenger**: Passenger information
- **driver**: Driver information (future assignment)
- **geo_coordinates**: Location data

### Booking Status Flow
```
ASSIGNING_DRIVER → CAB_ARRIVED → IN_RIDE → COMPLETED
                ↓
            CANCELLED
```

---

## 🐛 Troubleshooting

### "Passenger not found" Error
**Solution:** Ensure passenger exists in database:
```sql
SELECT * FROM passenger WHERE id = 1;
```

### "Connection refused" to Location Service
**Solution:** Ensure Location Service is running:
```bash
curl http://localhost:7477/api/location/nearby/drivers -X POST \
  -H "Content-Type: application/json" \
  -d '{"latitude": 28.7041, "longitude": 77.1025}'
```

### Entity Service Dependency Not Found
**Solution:** Rebuild and publish Entity Service:
```bash
cd ../Uber-Entity-Service
./gradlew clean build publishToMavenLocal -x test
```

### IDE Package Errors
**Solution:** Reload Gradle project:
```bash
./gradlew clean build --refresh-dependencies
```

---

## 📈 Future Enhancements

### Phase 2: Real-time Driver Assignment
- [ ] Integrate with Client Socket Service
- [ ] Send ride requests to all nearby drivers
- [ ] Implement first-accept-first-served logic
- [ ] Notify passenger of driver assignment

### Phase 3: Resilience Patterns
- [ ] Add circuit breaker (Resilience4j)
- [ ] Add retry logic with exponential backoff
- [ ] Add fallback mechanism for service failures
- [ ] Implement request timeout handling

### Phase 4: Advanced Features
- [ ] Fare estimation before booking
- [ ] Surge pricing based on demand
- [ ] Driver rating filter (4+ stars)
- [ ] Scheduled rides
- [ ] Multi-stop rides

### Phase 5: Observability
- [ ] Add Prometheus metrics
- [ ] Add distributed tracing (Zipkin/Jaeger)
- [ ] Add custom health checks
- [ ] Add performance monitoring

---

## 📝 Best Practices

1. ✅ **Configuration Package**: All `@Configuration` classes organized
2. ✅ **Externalized Config**: Service URLs in properties file
3. ✅ **Proper Logging**: Slf4j with meaningful messages
4. ✅ **Error Handling**: Try-catch for external calls
5. ✅ **DTO Pattern**: Separate request/response objects
6. ✅ **Repository Pattern**: JPA for database access
7. ✅ **Service Layer**: Business logic separated from controllers
8. ✅ **Documentation**: Comprehensive API flow documentation

---

## 🔗 Related Services

- [Uber-Entity-Service](../Uber-Entity-Service/README.md) - Shared domain models
- [Uber-Location-Service](../Uber-Location-Service/README.md) - Geospatial queries
- [Uber-Client-Socket-Service](../Uber-Client-Socket-Service/README.md) - WebSocket communication

---

## 📚 Documentation

- [API Flow Documentation](docs/API_FLOW.md) - Detailed end-to-end flow
- [Architecture Diagram](../Uber-Location-Service/docs/ARCHITECTURE.md) - Overall system design

---

## 👨‍💻 Development

### Tech Stack
- **Framework**: Spring Boot 4.0.1
- **Language**: Java 17
- **Build Tool**: Gradle 9.2.1
- **Database**: MySQL 9.5
- **ORM**: Spring Data JPA / Hibernate
- **HTTP Client**: RestTemplate

### Port
- **7475** (Production)

---

**Author:** Girik Garg  
**Last Updated:** January 7, 2026  
**Version:** 1.0.0


