#!/bin/bash

# Location Service API Test Script
# Usage: ./test-location-service.sh

BASE_URL="http://localhost:7477/api/location"
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo "🚀 Testing Uber Location Service"
echo "================================="
echo

# Test 1: Add drivers
echo -e "${BLUE}📍 Test 1: Adding drivers...${NC}"
echo

curl -s -X POST $BASE_URL/drivers \
  -H "Content-Type: application/json" \
  -d '{"driverId": "driver-123", "latitude": 28.7041, "longitude": 77.1025}' && echo -e " ${GREEN}✅ Driver 123 added (Connaught Place)${NC}"

curl -s -X POST $BASE_URL/drivers \
  -H "Content-Type: application/json" \
  -d '{"driverId": "driver-456", "latitude": 28.7041, "longitude": 77.1075}' && echo -e " ${GREEN}✅ Driver 456 added (India Gate area)${NC}"

curl -s -X POST $BASE_URL/drivers \
  -H "Content-Type: application/json" \
  -d '{"driverId": "driver-789", "latitude": 28.7100, "longitude": 77.1100}' && echo -e " ${GREEN}✅ Driver 789 added (Janpath area)${NC}"

curl -s -X POST $BASE_URL/drivers \
  -H "Content-Type: application/json" \
  -d '{"driverId": "driver-101", "latitude": 28.7200, "longitude": 77.1150}' && echo -e " ${GREEN}✅ Driver 101 added (Rajiv Chowk area)${NC}"

curl -s -X POST $BASE_URL/drivers \
  -H "Content-Type: application/json" \
  -d '{"driverId": "driver-999", "latitude": 28.4595, "longitude": 77.0266}' && echo -e " ${GREEN}✅ Driver 999 added (Gurgaon - far away)${NC}"

echo
sleep 1

# Test 2: Find nearby drivers
echo -e "${BLUE}🔍 Test 2: Finding nearby drivers (5km radius)...${NC}"
echo "Searching near Connaught Place (28.7041, 77.1025):"
curl -s -X POST $BASE_URL/nearby/drivers \
  -H "Content-Type: application/json" \
  -d '{"latitude": 28.7041, "longitude": 77.1025}' | jq

echo

# Test 3: Search far location
echo -e "${BLUE}🔍 Test 3: Searching far location (should be empty)...${NC}"
echo "Searching at (30.0, 78.0) - Far North:"
curl -s -X POST $BASE_URL/nearby/drivers \
  -H "Content-Type: application/json" \
  -d '{"latitude": 30.0, "longitude": 78.0}' | jq

echo

# Test 4: Verify with Redis CLI
echo -e "${BLUE}🔧 Test 4: Verifying with Redis CLI...${NC}"
echo "Drivers in Redis:"
redis-cli ZRANGE drivers 0 -1 2>/dev/null || echo "⚠️  Redis CLI not available"

echo
echo

# Test 5: GEORADIUS verification
echo -e "${BLUE}📏 Test 5: Redis GEORADIUS (with distances)...${NC}"
echo "Finding drivers within 5km of Connaught Place (77.1025, 28.7041):"
redis-cli GEORADIUS drivers 77.1025 28.7041 5 km WITHCOORD WITHDIST 2>/dev/null || echo "⚠️  Redis CLI not available"

echo
echo "================================="
echo -e "${GREEN}✅ All tests completed!${NC}"
echo
echo "📊 Summary:"
echo "  - Added 5 drivers (4 nearby, 1 far)"
echo "  - Tested nearby search (5km radius)"
echo "  - Verified with Redis commands"
echo
echo "🧹 To clear all data: redis-cli DEL drivers"

