# Uber Entity Service

A shared library microservice containing common data models (entities) used across multiple services in the ride-hailing platform.

## Purpose

This service defines the core database entities that are shared between different microservices:
- **Booking** - Ride booking information
- **Driver** - Driver profile and details
- **Passenger** - Passenger profile and details

By centralizing these entities, we ensure consistency across all services and avoid code duplication.

## Technology Stack

- **Spring Boot 3.2.5**
- **Spring Data JPA** - Database operations
- **Flyway** - Database migrations
- **MySQL** - Database
- **Lombok** - Reduces boilerplate code

## Publishing to Maven Local

To make this entity service available for other microservices to use as a dependency, run:

```bash
./gradlew publishToMavenLocal
```

This command publishes the compiled JAR to your local Maven repository (`~/.m2/repository`), allowing other services in the project to import and use these entities.

## Using in Other Services

After publishing, other services can use this entity service by adding the following to their `build.gradle`:

```gradle
repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation 'com.girikgarg:uber-entity-service:0.0.1-SNAPSHOT'
}
```

Then import the entities:

```java
import com.girikgarg.uberentityservice.models.Booking;
import com.girikgarg.uberentityservice.models.Driver;
import com.girikgarg.uberentityservice.models.Passenger;
```

## Database Configuration

The service uses Flyway for database migrations. Ensure MySQL is running and configured in `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/Uber_Db_Local
spring.datasource.username=root
spring.datasource.password=your_password
```

## Running the Service

To run the application:

```bash
./gradlew bootRun
```

This will:
1. Start the Spring Boot application
2. Execute Flyway migrations (create database tables)
3. Validate entity mappings

## Build & Test

```bash
# Clean and build
./gradlew clean build

# Run without tests
./gradlew clean build -x test

# Publish to Maven Local
./gradlew publishToMavenLocal
```

## Database Schema

The service creates the following tables via Flyway migrations:
- `booking` - Stores ride booking records
- `driver` - Stores driver information
- `passenger` - Stores passenger information
- `flyway_schema_history` - Tracks migration history

## Notes

- This is a shared library service, not a standalone REST API
- Always publish to Maven Local after making entity changes
- Other services depend on this for data models