# 🚀 Uber Microservices Platform

**Production-Grade Microservices Architecture | Local Development | AWS Deployment | Demo Ready**

---

## 📖 Single Source of Truth

**All documentation has been consolidated into one comprehensive guide:**

👉 **[AWS-DEPLOYMENT-GUIDE.md](./AWS-DEPLOYMENT-GUIDE.md)** 👈

This guide contains:
- ✅ **Part 1: Local Development & Demo**
  - Quick start instructions
  - Complete demo guide (24-minute walkthrough)
  - Automation scripts
  - Architecture diagrams
  - Testing commands

- ✅ **Part 2: AWS Production Deployment**
  - Infrastructure setup (VPC, RDS, Redis, Kafka, EC2)
  - Application deployment
  - Production demo with CloudWatch logs
  - Cleanup instructions
  - Resume bullet points

---

## 🚀 Quick Start (Local Development)

### Prerequisites
- Java 17
- MySQL (port 3306)
- Redis (port 6379)
- Kafka (port 9092)
- Gradle

### Option 1: Automated Start (Recommended)

```bash
# Make scripts executable (first time only)
chmod +x start-all-services.sh stop-all-services.sh

# Start all services
./start-all-services.sh

# When done
./stop-all-services.sh
```

### Option 2: Manual Start

See [AWS-DEPLOYMENT-GUIDE.md](./AWS-DEPLOYMENT-GUIDE.md#quick-start---local-development) for manual startup instructions.

---

## 🏗️ Architecture Overview

### **Microservices (7 total):**
1. **Uber-API-Gateway** (Port 9000) - Single entry point, JWT auth delegation
2. **Uber-Service-Discovery** (Port 8761) - Eureka server
3. **Uber-Auth-Service** (Port 9090) - JWT creation & validation
4. **Uber-Booking-Service** (Port 8001) - Booking management + Kafka consumer
5. **Uber-Location-Service** (Port 7070) - Redis geospatial queries
6. **Uber-Socket-Service** (Port 3002) - WebSocket + Kafka producer
7. **Uber-Review-Service** (Port 7272) - Reviews CRUD

### **Key Technical Highlights:**

🔐 **Proper API Gateway Pattern**
- Gateway delegates JWT validation to Auth Service (not local validation)
- Auth Service is the single source of truth for authentication
- Gateway calls Auth Service's `/validate` endpoint for every protected request

🌐 **Service Discovery**
- Eureka enables dynamic service resolution
- No hardcoded IPs or ports
- Client-side load balancing

📨 **Event-Driven Architecture**
- Kafka for asynchronous communication
- Socket Service → Booking Service decoupled via Kafka
- Guaranteed message delivery

⚡ **Real-Time Communication**
- WebSocket for instant driver notifications
- Redis GEORADIUS for sub-second nearby driver search

---

## 🎬 Demo Flow (24 minutes)

1. **Setup** (3 min) - Show Eureka dashboard with all services
2. **Authentication** (5 min) - Signup → Signin → JWT validation (Gateway → Auth Service)
3. **Location Service** (5 min) - Update driver locations, Redis GEORADIUS query
4. **Booking Service** (7 min) - Create booking, inter-service communication
5. **WebSocket** (10 min) - Real-time broadcast to drivers
6. **Kafka Event Flow** (10 min) - Driver accepts ride, Kafka async processing
7. **Review Service** (5 min) - Complete ride, leave review

**Full demo script with cURL commands:** [AWS-DEPLOYMENT-GUIDE.md](./AWS-DEPLOYMENT-GUIDE.md#complete-demo-guide)

---

## 🎯 Technical Highlights

✨ **Enterprise Microservices Patterns**
- API Gateway delegates authentication (not local validation)
- Single source of truth for JWT (Auth Service)
- Service discovery with Eureka (no hardcoded endpoints)

✨ **Production-Ready Architecture**
- Event-driven with Kafka
- Real-time with WebSocket
- Geospatial queries with Redis
- Centralized logging ready

✨ **Cloud-Native Deployment**
- VPC with public/private subnets
- RDS, ElastiCache, MSK
- ALB, NAT Gateway, CloudWatch
- Security groups enforcing zero direct backend access

---

## 📚 Documentation Structure

```
Microservices-Based Ride-Hailing Platform/
├── README.md                          # This file (overview & quick start)
├── AWS-DEPLOYMENT-GUIDE.md            # 📖 SINGLE SOURCE OF TRUTH
│                                      #    - Local development guide
│                                      #    - Complete demo walkthrough
│                                      #    - AWS deployment instructions
│                                      #    - Architecture diagrams
│                                      #    - Resume bullet points
│
├── start-all-services.sh              # Automated startup script
├── stop-all-services.sh               # Automated shutdown script
│
├── Uber-API-Gateway/                  # API Gateway (delegates auth to Auth Service)
├── Uber-Service-Discovery/            # Eureka server
├── Uber-Auth-Service/                 # JWT creation & validation
├── Uber-Booking-Service/              # Booking management + Kafka consumer
├── Uber-Location-Service/             # Redis geospatial
├── Uber-Socket-Service/               # WebSocket + Kafka producer
├── Uber-Review-Service/               # Reviews CRUD
├── Uber-Entity-Service/               # Shared entities (dependency)
│
├── Uber-Driver-WebSocket-Client/      # Test client for WebSocket
└── Uber-WebSocket-Demo-Client/        # Demo client for WebSocket
```

---

## 🔑 Key Architecture Decisions

### **API Gateway Authentication Pattern**

❌ **WRONG (Anti-pattern):**
```
Gateway validates JWT locally
→ JWT logic duplicated in Gateway
→ Changes require Gateway redeployment
```

✅ **CORRECT (Our Implementation):**
```
Gateway calls Auth Service /validate endpoint
→ Auth Service is single source of truth
→ JWT logic centralized in Auth Service
→ Changes don't require Gateway redeployment
```

**Flow:**
```
Client → Gateway (extract JWT) 
       → Auth Service /validate (validate JWT)
       → Gateway (add X-User-Email header)
       → Target Service (trusts Gateway)
```

---

## 🚀 Technologies Used

- **Backend:** Spring Boot, Spring Cloud Gateway, Spring Security, Java 17
- **Service Discovery:** Eureka
- **Databases:** MySQL (RDS), Redis (ElastiCache)
- **Messaging:** Apache Kafka (MSK)
- **Real-Time:** WebSocket
- **Cloud:** AWS (EC2, VPC, RDS, ElastiCache, MSK, ALB, NAT Gateway, CloudWatch)
- **Build:** Gradle
- **Migration:** Flyway

---

## 📝 Resume-Ready Bullet Points

See [AWS-DEPLOYMENT-GUIDE.md](./AWS-DEPLOYMENT-GUIDE.md#-resume-bullet-points) for polished resume bullet points highlighting:
- API Gateway authentication delegation pattern
- Event-driven architecture with Kafka
- Microservices orchestration with Eureka
- AWS production deployment
- Real-time systems with WebSocket
- Geospatial queries with Redis

---

## 🎯 Next Steps

1. **Local Demo:** Follow [Quick Start](#-quick-start-local-development) to run locally
2. **Complete Demo:** Use [Demo Guide](./AWS-DEPLOYMENT-GUIDE.md#complete-demo-guide) for 24-minute walkthrough
3. **AWS Deployment:** Follow [AWS Deployment](./AWS-DEPLOYMENT-GUIDE.md#part-2-aws-production-deployment) for production setup
4. **Interview Prep:** Review [Architecture Decisions](./AWS-DEPLOYMENT-GUIDE.md#-architecture) and [Key Highlights](./AWS-DEPLOYMENT-GUIDE.md#-key-demo-highlights-to-emphasize)

---

## 💡 Key Technical Achievements

**Microservices Architecture:**
- ✅ Built 7 production-grade microservices with proper separation of concerns
- ✅ Implemented API Gateway authentication delegation pattern
- ✅ Deployed to AWS with enterprise-grade security architecture
- ✅ End-to-end observability with comprehensive logging

**Advanced Patterns:**
- ✅ Event-driven architecture using Apache Kafka
- ✅ Real-time communication with WebSocket
- ✅ Service discovery and load balancing with Eureka
- ✅ Geospatial queries with Redis for location-based services

---

**Ready to showcase your skills? Start with [AWS-DEPLOYMENT-GUIDE.md](./AWS-DEPLOYMENT-GUIDE.md)!** 🚀
