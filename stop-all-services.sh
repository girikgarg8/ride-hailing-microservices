#!/bin/bash

# 🛑 Stop All Uber Microservices

# Colors for output
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${RED}╔════════════════════════════════════════════════════════════════════╗${NC}"
echo -e "${RED}║                                                                    ║${NC}"
echo -e "${RED}║         🛑 STOPPING UBER MICROSERVICES PLATFORM 🛑                ║${NC}"
echo -e "${RED}║                                                                    ║${NC}"
echo -e "${RED}╚════════════════════════════════════════════════════════════════════╝${NC}"
echo ""

# Function to stop a service
stop_service() {
    local service_name=$1
    local pid_file="/tmp/$service_name.pid"
    
    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        echo -e "${BLUE}Stopping $service_name (PID: $pid)...${NC}"
        kill -9 $pid 2>/dev/null || echo -e "${RED}   Process not found${NC}"
        rm "$pid_file"
    else
        echo -e "${RED}No PID file found for $service_name${NC}"
    fi
}

# Stop all services
stop_service "Uber-API-Gateway"
stop_service "Uber-Review-Service"
stop_service "Uber-Socket-Service"
stop_service "Uber-Location-Service"
stop_service "Uber-Booking-Service"
stop_service "Uber-Auth-Service"
stop_service "Uber-Service-Discovery"

# Also kill any remaining gradle processes
echo ""
echo -e "${BLUE}Aggressively killing all remaining processes...${NC}"
pkill -9 -f "gradlew bootRun" 2>/dev/null
pkill -9 -f "UberBookingService" 2>/dev/null
pkill -9 -f "UberLocationService" 2>/dev/null
pkill -9 -f "UberSocketService" 2>/dev/null
pkill -9 -f "UberAuthService" 2>/dev/null
pkill -9 -f "UberApiGateway" 2>/dev/null
sleep 2

echo ""
echo -e "${RED}╔════════════════════════════════════════════════════════════════════╗${NC}"
echo -e "${RED}║                                                                    ║${NC}"
echo -e "${RED}║         ✅ ALL SERVICES STOPPED SUCCESSFULLY! ✅                   ║${NC}"
echo -e "${RED}║                                                                    ║${NC}"
echo -e "${RED}╚════════════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${BLUE}📝 Logs are still available in: /tmp/<service-name>.log${NC}"
echo ""
