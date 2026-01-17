#!/bin/bash

# 🚀 Start All Uber Microservices
# This script starts all services in the correct order for the demo

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}╔════════════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║                                                                    ║${NC}"
echo -e "${BLUE}║         🚀 STARTING UBER MICROSERVICES PLATFORM 🚀                ║${NC}"
echo -e "${BLUE}║                                                                    ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════════════╝${NC}"
echo ""

# STEP 0: Clean up any existing processes
echo -e "${YELLOW}═══════════════════════════════════════════════════════════════════${NC}"
echo -e "${YELLOW}STEP 0: Cleaning up any existing processes...${NC}"
echo -e "${YELLOW}═══════════════════════════════════════════════════════════════════${NC}"
pkill -9 -f "gradlew bootRun" 2>/dev/null
sleep 2
echo -e "${GREEN}✓ Cleanup complete${NC}"
echo ""

# Set Java 17
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
echo -e "${BLUE}Using Java: $(java -version 2>&1 | head -1)${NC}"
echo ""

# Function to start a service and wait for startup
start_service() {
    local service_name=$1
    local service_dir=$2
    local service_port=$3
    local max_wait=$4  # Max wait time in seconds
    
    echo -e "${GREEN}➡️  Starting $service_name on port $service_port...${NC}"
    cd "$service_dir"
    ./gradlew bootRun > "/tmp/$service_name.log" 2>&1 &
    local pid=$!
    echo "$pid" > "/tmp/$service_name.pid"
    echo -e "${BLUE}   PID: $pid${NC}"
    
    # Wait for service to start (check health OR logs)
    local elapsed=0
    local interval=2
    while [ $elapsed -lt $max_wait ]; do
        # Try health endpoint first
        if curl -s --max-time 1 http://localhost:$service_port/actuator/health 2>/dev/null | grep -q "UP"; then
            echo -e "${GREEN}   ✓ $service_name is UP! (took ${elapsed}s)${NC}"
            echo ""
            return 0
        fi
        
        # If no actuator, check logs for "Started" message
        if tail -50 "/tmp/$service_name.log" 2>/dev/null | grep -q "Started.*Application.*in"; then
            echo -e "${GREEN}   ✓ $service_name started! (took ${elapsed}s)${NC}"
            echo ""
            return 0
        fi
        
        sleep $interval
        elapsed=$((elapsed + interval))
        if [ $((elapsed % 6)) -eq 0 ]; then
            echo -e "${BLUE}   ... waiting (${elapsed}s)${NC}"
        fi
    done
    
    # Service didn't start in time - show logs
    echo -e "${RED}   ✗ $service_name failed to start within ${max_wait}s${NC}"
    echo -e "${YELLOW}   Last 30 lines of log:${NC}"
    tail -30 "/tmp/$service_name.log" | grep -E "ERROR|Exception|Failed|Caused by" | head -15 || tail -15 "/tmp/$service_name.log"
    echo ""
    return 1
}

# Get the base directory
BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Step 1: Start Eureka Server (must start first!)
echo -e "${GREEN}═══════════════════════════════════════════════════════════════════${NC}"
echo -e "${GREEN}STEP 1: Starting Service Discovery (Eureka)${NC}"
echo -e "${GREEN}═══════════════════════════════════════════════════════════════════${NC}"
start_service "Uber-Service-Discovery" "$BASE_DIR/Uber-Service-Discovery" "8761" "30" || exit 1

# Step 2: Start Auth Service
echo -e "${GREEN}═══════════════════════════════════════════════════════════════════${NC}"
echo -e "${GREEN}STEP 2: Starting Auth Service${NC}"
echo -e "${GREEN}═══════════════════════════════════════════════════════════════════${NC}"
start_service "Uber-Auth-Service" "$BASE_DIR/Uber-Auth-Service" "9090" "30" || exit 1

# Step 3: Start Location Service (needed by Booking)
echo -e "${GREEN}═══════════════════════════════════════════════════════════════════${NC}"
echo -e "${GREEN}STEP 3: Starting Location Service${NC}"
echo -e "${GREEN}═══════════════════════════════════════════════════════════════════${NC}"
start_service "Uber-Location-Service" "$BASE_DIR/Uber-Location-Service" "7477" "30" || exit 1

# Step 4: Start Socket Service (needed by Booking)
echo -e "${GREEN}═══════════════════════════════════════════════════════════════════${NC}"
echo -e "${GREEN}STEP 4: Starting Socket Service${NC}"
echo -e "${GREEN}═══════════════════════════════════════════════════════════════════${NC}"
start_service "Uber-Socket-Service" "$BASE_DIR/Uber-Socket-Service" "8080" "30" || exit 1

# Step 5: Start Review Service
echo -e "${GREEN}═══════════════════════════════════════════════════════════════════${NC}"
echo -e "${GREEN}STEP 5: Starting Review Service${NC}"
echo -e "${GREEN}═══════════════════════════════════════════════════════════════════${NC}"
start_service "Uber-Review-Service" "$BASE_DIR/Uber-Review-Service" "7272" "30" || exit 1

# Step 6: Start Booking Service (depends on Location & Socket)
echo -e "${GREEN}═══════════════════════════════════════════════════════════════════${NC}"
echo -e "${GREEN}STEP 6: Starting Booking Service${NC}"
echo -e "${GREEN}═══════════════════════════════════════════════════════════════════${NC}"
start_service "Uber-Booking-Service" "$BASE_DIR/Uber-Booking-Service" "7475" "30" || exit 1

# Step 7: Start API Gateway (must start last!)
echo -e "${GREEN}═══════════════════════════════════════════════════════════════════${NC}"
echo -e "${GREEN}STEP 7: Starting API Gateway${NC}"
echo -e "${GREEN}═══════════════════════════════════════════════════════════════════${NC}"
start_service "Uber-API-Gateway" "$BASE_DIR/Uber-API-Gateway" "9001" "30" || exit 1

echo -e "${BLUE}╔════════════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║                                                                    ║${NC}"
echo -e "${BLUE}║         ✅ ALL SERVICES STARTED SUCCESSFULLY! ✅                   ║${NC}"
echo -e "${BLUE}║                                                                    ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${GREEN}📊 SERVICE ENDPOINTS:${NC}"
echo -e "${BLUE}   Eureka Dashboard:    http://localhost:8761${NC}"
echo -e "${BLUE}   API Gateway:         http://localhost:9001${NC}"
echo -e "${BLUE}   Auth Service:        http://localhost:9090${NC}"
echo -e "${BLUE}   Booking Service:     http://localhost:7475${NC}"
echo -e "${BLUE}   Location Service:    http://localhost:7477${NC}"
echo -e "${BLUE}   Socket Service:      http://localhost:8080${NC}"
echo -e "${BLUE}   Review Service:      http://localhost:7272${NC}"
echo ""
echo -e "${GREEN}🔍 QUICK HEALTH CHECK:${NC}"
echo -e "${BLUE}   Gateway Health:      http://localhost:9001/actuator/health${NC}"
echo -e "${BLUE}   Gateway Routes:      http://localhost:9001/actuator/gateway/routes${NC}"
echo ""
echo -e "${GREEN}📝 LOGS LOCATION:${NC}"
echo -e "${BLUE}   All logs are in: /tmp/Uber-<service-name>.log${NC}"
echo ""
echo -e "${GREEN}🛑 TO STOP ALL SERVICES:${NC}"
echo -e "${BLUE}   Run: ./stop-all-services.sh${NC}"
echo ""
echo -e "${GREEN}🚀 READY FOR DEMO!${NC}"
echo -e "${GREEN}   See AWS-DEPLOYMENT-GUIDE.md for step-by-step demo instructions${NC}"
echo ""
