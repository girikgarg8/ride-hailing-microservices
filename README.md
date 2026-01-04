# 🚖 Microservices-Based Ride-Hailing Platform

A production-ready, distributed ride-hailing platform built using **microservices architecture** with **Spring Boot**, demonstrating industry best practices for scalable backend systems.

---

## 🏗️ Architecture Overview

This project implements a complete ride-hailing system (Uber-like) using modern microservices patterns:

- **Shared Entity Library Pattern** - Common data models
- **Database Per Service** - Independent data storage
- **API Gateway** - Unified entry point
- **Service Discovery** - Dynamic service registration
- **Event-Driven Architecture** - Asynchronous communication
- **Distributed Transactions** - SAGA pattern
- **Real-Time Features** - WebSockets for notifications
- **Geospatial Capabilities** - Location-based services

---

## 📦 Microservices

### ✅ 1. Uber Entity Service
**Status:** Complete | **Type:** Shared Library

Common data models (entities) shared across all microservices.

**Features:**
- Core entities: `Driver`, `Passenger`, `Booking`, `BaseModel`
- JPA/Hibernate mappings with relationships
- Flyway database migrations (V1 with FK constraints)
- Published to Maven Local for cross-service consumption
- Lombok integration for boilerplate reduction

**Tech Stack:**
- Spring Boot 3.2.5, Spring Data JPA
- MySQL 8.0+, Flyway
- Lombok, Java 17

📂 [View Service](./Uber-Entity-Service/)

---

### ✅ 2. Demo Entity Consumer Service
**Status:** Complete | **Type:** Reference Implementation

Demonstrates how to integrate and consume the Entity Service in microservices.

**Features:**
- Imports `uber-entity-service` from Maven Local
- JPA Repositories: `DriverRepository`, `PassengerRepository`, `BookingRepository`
- REST API endpoints for CRUD operations
- Proper dependency management

**Tech Stack:**
- Spring Boot 3.2.5, Spring Web
- Maven Local dependency resolution

📂 [View Service](./Demo-Entity-Consumer-Service/)

---

### 🔜 3. Auth Service (Planned)
Authentication and authorization microservice.

**Planned Features:**
- JWT-based authentication
- OAuth2 integration
- Role-based access control (RBAC)
- Passenger & Driver registration/login

---

### 🔜 4. Booking Service (Planned)
Core booking management service.

**Planned Features:**
- Create and manage bookings
- Real-time driver assignment
- Booking status tracking
- Fare calculation

---

### 🔜 5. Location Service (Planned)
Geospatial and location tracking service.

**Planned Features:**
- Real-time location tracking
- Geospatial search for nearby drivers
- Route optimization
- Redis for caching live locations

---

### 🔜 6. Payment Service (Planned)
Payment processing and wallet management.

**Planned Features:**
- Payment gateway integration
- Digital wallet system
- Transaction history
- Refund processing

---

### 🔜 7. Notification Service (Planned)
Real-time notifications and alerts.

**Planned Features:**
- WebSocket-based real-time updates
- SMS/Email notifications
- Push notifications
- Event-driven messaging (Kafka/RabbitMQ)

---

## 🛠️ Tech Stack

| Layer | Technologies |
|-------|-------------|
| **Backend** | Spring Boot 3.2.5, Java 17 |
| **Database** | MySQL 8.0+, Flyway |
| **Build Tool** | Gradle 8.7 |
| **ORM** | Spring Data JPA, Hibernate |
| **Service Discovery** | Eureka (Planned) |
| **API Gateway** | Spring Cloud Gateway (Planned) |
| **Messaging** | Kafka/RabbitMQ (Planned) |
| **Caching** | Redis (Planned) |
| **Monitoring** | Prometheus, Grafana (Planned) |

---

## 🚀 Getting Started

### Prerequisites
```bash
- Java 17+
- MySQL 8.0+
- Gradle 8.x
```

### 1. Setup MySQL Database
```bash
mysql -u root -p
CREATE DATABASE Uber_Db_Local;
```

### 2. Build & Publish Entity Service
```bash
cd Uber-Entity-Service
./gradlew clean build publishToMavenLocal -x test
```

### 3. Run Demo Consumer Service
```bash
cd Demo-Entity-Consumer-Service
./gradlew bootRun
```

### 4. Test API
```bash
# Get all drivers
curl http://localhost:8888/api/demo/drivers

# Get all passengers
curl http://localhost:8888/api/demo/passengers
```

---

## 📊 Database Schema

### Current Schema (V1)

```
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│    passenger    │       │     booking     │       │     driver      │
├─────────────────┤       ├─────────────────┤       ├─────────────────┤
│ id (PK)         │       │ id (PK)         │       │ id (PK)         │
│ name            │◄──────│ passenger_id(FK)│       │ name            │
│ email           │       │ driver_id (FK)  │──────►│ license_number  │
│ phone_number    │       │ booking_status  │       │ govt_id_number  │
│ password        │       │ start_time      │       │ phone_number    │
│ created_at      │       │ end_time        │       │ created_at      │
│ updated_at      │       │ total_distance  │       │ updated_at      │
└─────────────────┘       │ created_at      │       └─────────────────┘
                          │ updated_at      │
                          └─────────────────┘
```

---

## 🗂️ Project Structure

```
Microservices-Based-Ride-Hailing-Platform/
├── README.md                           # This file
├── .gitignore                          # Git ignore rules
│
├── Uber-Entity-Service/                # Shared entity library
│   ├── src/main/java/.../models/       # Entity models
│   ├── src/main/resources/
│   │   ├── application.properties      # DB config
│   │   └── db/migration/               # Flyway migrations
│   ├── build.gradle                    # Gradle build file
│   └── README.md                       # Service documentation
│
├── Demo-Entity-Consumer-Service/       # Reference implementation
│   ├── src/main/java/.../repository/   # JPA repositories
│   ├── src/main/java/.../controller/   # REST controllers
│   ├── build.gradle                    # Gradle with mavenLocal
│   └── README.md                       # Service documentation
│
└── [Future Services]
    ├── auth-service/
    ├── booking-service/
    ├── location-service/
    ├── payment-service/
    └── notification-service/
```

---

## 🎯 Development Workflow

### Adding a New Service

1. **Create service directory** in root
2. **Add dependency** on `uber-entity-service` in `build.gradle`:
   ```gradle
   dependencies {
       implementation 'com.girikgarg:uber-entity-service:0.0.1-SNAPSHOT'
   }
   
   repositories {
       mavenLocal()
       mavenCentral()
   }
   ```
3. **Configure** `application.properties` with DB credentials
4. **Import entities** and create repositories

### Making Changes to Entities

1. **Update** `Uber-Entity-Service` entities
2. **Create migration** (e.g., `V2__add_new_field.sql`)
3. **Rebuild & republish**: `./gradlew publishToMavenLocal`
4. **Restart** consumer services to pick up changes

---

## 🧪 Testing

### Entity Service
```bash
cd Uber-Entity-Service
./gradlew test
./gradlew bootRun  # Verify migrations run successfully
```

### Demo Consumer Service
```bash
cd Demo-Entity-Consumer-Service
./gradlew test
./gradlew bootRun
curl http://localhost:8888/api/demo/health
```

---

## 📝 Key Features Implemented

- ✅ Shared entity library with Maven Local publishing
- ✅ Flyway database migrations with FK constraints
- ✅ JPA entities with proper relationships
- ✅ Lombok for clean, concise code
- ✅ Spring Boot 3.2.5 with Java 17
- ✅ RESTful API examples
- ✅ Repository pattern implementation
- ✅ Professional monorepo structure

---

## 🎓 Learning Outcomes

This project demonstrates:
- Microservices architecture and design patterns
- Spring Boot ecosystem mastery
- Database design and migrations
- Dependency management in distributed systems
- REST API design
- Version control and monorepo management
- Production-ready code practices

---

## 🤝 Contributing

This is a personal learning project. Feel free to fork and adapt for your own learning!

---

## 📄 License

This project is open source and available for educational purposes.

---

## 👨‍💻 Author

**Girik Garg**

- Focus: Backend Development, Microservices, Distributed Systems
- Stack: Java, Spring Boot, MySQL, Redis, Kafka

---

## 🚧 Roadmap

- [x] Entity Service with Flyway migrations
- [x] Demo Consumer Service
- [ ] Auth Service with JWT
- [ ] Booking Service with SAGA pattern
- [ ] Location Service with Redis caching
- [ ] Payment Service integration
- [ ] Notification Service with WebSockets
- [ ] API Gateway with Spring Cloud
- [ ] Service Discovery with Eureka
- [ ] Docker & Kubernetes deployment
- [ ] CI/CD pipeline
- [ ] Monitoring & Logging (ELK Stack)

---

**Built with ❤️ and ☕ | Microservices Architecture | Spring Boot**
