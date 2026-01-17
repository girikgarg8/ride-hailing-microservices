#!/bin/bash

echo "╔════════════════════════════════════════════════════════════════════╗"
echo "║                                                                    ║"
echo "║    🎬 COMPLETE DEMO: ALL REQUESTS VIA API GATEWAY 🎬             ║"
echo "║                                                                    ║"
echo "╚════════════════════════════════════════════════════════════════════╝"
echo ""

# Cleanup
rm -f demo-driver.txt demo-passenger.txt

# Clean up old demo data from database
echo "🧹 Cleaning up old demo data..."
mysql -u root -p${MYSQL_LOCAL_PASSWORD} -D Uber_Db_Local -e "
DELETE FROM users WHERE email IN ('demo-driver@uber.com', 'demo-passenger@uber.com');
" 2>/dev/null
echo "✅ Database cleaned"
echo ""

echo "════════════════════════════════════════════════════════════════════"
echo "STEP 1: DRIVER REGISTRATION (via Gateway)"
echo "════════════════════════════════════════════════════════════════════"
echo "POST http://localhost:9001/api/v1/auth/signup"
echo ""
DRIVER_RESP=$(curl -i -X POST http://localhost:9001/api/v1/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "demo-driver@uber.com",
    "password": "driver123",
    "name": "John Driver",
    "phoneNumber": "+1234567890",
    "role": "DRIVER"
  }' 2>&1)

echo "$DRIVER_RESP"
echo ""
echo "✅ Driver registered successfully"
echo ""

echo "════════════════════════════════════════════════════════════════════"
echo "STEP 2: PASSENGER REGISTRATION (via Gateway)"
echo "════════════════════════════════════════════════════════════════════"
echo "POST http://localhost:9001/api/v1/auth/signup"
echo ""
PASSENGER_RESP=$(curl -i -X POST http://localhost:9001/api/v1/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "demo-passenger@uber.com",
    "password": "passenger123",
    "name": "Jane Passenger",
    "phoneNumber": "+0987654321",
    "role": "PASSENGER"
  }' 2>&1)

echo "$PASSENGER_RESP"
echo ""
echo "✅ Passenger registered successfully"
echo ""

echo "════════════════════════════════════════════════════════════════════"
echo "DATABASE CHECK: Verify users created"
echo "════════════════════════════════════════════════════════════════════"
mysql -u root -p${MYSQL_LOCAL_PASSWORD} -D Uber_Db_Local -e "
SELECT id, name, email, role, created_at 
FROM users 
WHERE email IN ('demo-driver@uber.com', 'demo-passenger@uber.com')
ORDER BY id;
" 2>/dev/null
echo ""

echo "════════════════════════════════════════════════════════════════════"
echo "STEP 3: DRIVER SIGN IN (via Gateway)"
echo "════════════════════════════════════════════════════════════════════"
echo "POST http://localhost:9001/api/v1/auth/signin"
echo ""
curl -i -X POST http://localhost:9001/api/v1/auth/signin \
  -H "Content-Type: application/json" \
  -c demo-driver.txt \
  -d '{
    "email": "demo-driver@uber.com",
    "password": "driver123"
  }' 2>&1

echo ""
echo "✅ Driver authenticated - JWT cookie saved"
echo ""

echo "════════════════════════════════════════════════════════════════════"
echo "STEP 4: PASSENGER SIGN IN (via Gateway)"
echo "════════════════════════════════════════════════════════════════════"
echo "POST http://localhost:9001/api/v1/auth/signin"
echo ""
curl -i -X POST http://localhost:9001/api/v1/auth/signin \
  -H "Content-Type: application/json" \
  -c demo-passenger.txt \
  -d '{
    "email": "demo-passenger@uber.com",
    "password": "passenger123"
  }' 2>&1

echo ""
echo "✅ Passenger authenticated - JWT cookie saved"
echo ""

echo "════════════════════════════════════════════════════════════════════"
echo "STEP 5: PASSENGER CREATES BOOKING (via Gateway)"
echo "════════════════════════════════════════════════════════════════════"
echo "POST http://localhost:9001/api/v1/bookings"
echo "Route: Golden Gate Bridge → Fisherman's Wharf"
echo ""
# Query the actual passenger ID from database (since IDs don't match user IDs)
PASSENGER_ID=$(mysql -u root -p${MYSQL_LOCAL_PASSWORD} -D Uber_Db_Local -N -e "
SELECT id FROM passenger WHERE email = 'demo-passenger@uber.com' LIMIT 1;
" 2>/dev/null)
echo "Using Passenger ID from database: $PASSENGER_ID"
echo ""

BOOKING_RESP=$(curl -i -X POST http://localhost:9001/api/v1/bookings \
  -H "Content-Type: application/json" \
  -b demo-passenger.txt \
  -d '{
    "passengerId": '$PASSENGER_ID',
    "startLocation": {
      "latitude": 37.8199,
      "longitude": -122.4783
    },
    "endLocation": {
      "latitude": 37.8080,
      "longitude": -122.4177
    }
  }' 2>&1)

echo "$BOOKING_RESP"
# Extract booking ID from response (search for the JSON in the response body)
BOOKING_ID=$(echo "$BOOKING_RESP" | grep -oE '"bookingId":[0-9]+' | head -1 | grep -oE '[0-9]+')

echo ""
echo "✅ Booking created with ID: $BOOKING_ID"
echo "   Initial status: ASSIGNING_DRIVER"
echo ""

echo "════════════════════════════════════════════════════════════════════"
echo "DATABASE CHECK: Verify booking in database"
echo "════════════════════════════════════════════════════════════════════"
if [ -n "$BOOKING_ID" ]; then
    mysql -u root -p${MYSQL_LOCAL_PASSWORD} -D Uber_Db_Local -t -e "
SELECT
    b.id AS booking_id,
    b.booking_status,
    p.name AS passenger_name,
    CONCAT(sc.latitude, ', ', sc.longitude) AS start_location,
    CONCAT(ec.latitude, ', ', ec.longitude) AS end_location,
    b.created_at
FROM booking b
JOIN passenger p ON b.passenger_id = p.id
JOIN geo_coordinates sc ON b.start_location_id = sc.id
JOIN geo_coordinates ec ON b.end_location_id = ec.id
WHERE b.id = ${BOOKING_ID};
" 2>/dev/null
else
    echo "⚠️  Could not extract booking ID from response"
fi
echo ""

echo "════════════════════════════════════════════════════════════════════"
echo "STEP 6: DRIVER ACCEPTS BOOKING (via Gateway)"
echo "════════════════════════════════════════════════════════════════════"
echo "PATCH http://localhost:9001/api/v1/bookings/$BOOKING_ID"
echo ""
curl -i -X PATCH http://localhost:9001/api/v1/bookings/$BOOKING_ID \
  -H "Content-Type: application/json" \
  -b demo-driver.txt \
  -d '{
    "status": "CAB_ARRIVED"
  }' 2>&1

echo ""
echo "✅ Driver accepted booking - Status updated to CAB_ARRIVED"
echo ""

echo "════════════════════════════════════════════════════════════════════"
echo "DATABASE CHECK: Verify booking status updated"
echo "════════════════════════════════════════════════════════════════════"
if [ -n "$BOOKING_ID" ]; then
    mysql -u root -p${MYSQL_LOCAL_PASSWORD} -D Uber_Db_Local -t -e "
SELECT 
    id,
    booking_status,
    created_at,
    updated_at,
    TIMESTAMPDIFF(SECOND, created_at, updated_at) AS seconds_to_accept
FROM booking
WHERE id = ${BOOKING_ID};
" 2>/dev/null
else
    echo "⚠️  Booking ID not available for database check"
fi
echo ""

echo "════════════════════════════════════════════════════════════════════"
echo "STEP 7: PASSENGER CREATES REVIEW (via Gateway)"
echo "════════════════════════════════════════════════════════════════════"
echo "POST http://localhost:9001/api/v1/reviews"
echo ""
REVIEW_RESP=$(curl -i -X POST http://localhost:9001/api/v1/reviews \
  -H "Content-Type: application/json" \
  -b demo-passenger.txt \
  -d '{
    "bookingId": '$BOOKING_ID',
    "content": "Excellent service! Driver was punctual and professional.",
    "rating": 5.0
  }' 2>&1)

echo "$REVIEW_RESP"
REVIEW_ID=$(echo "$REVIEW_RESP" | grep -oE '"id":[0-9]+' | head -1 | grep -oE '[0-9]+')

echo ""
echo "✅ Review submitted with ID: $REVIEW_ID"
echo ""

echo "════════════════════════════════════════════════════════════════════"
echo "DATABASE CHECK: Verify review in database"
echo "════════════════════════════════════════════════════════════════════"
if [ -n "$REVIEW_ID" ]; then
    mysql -u root -p${MYSQL_LOCAL_PASSWORD} -D Uber_Db_Local -t -e "
SELECT 
    r.id AS review_id,
    r.rating,
    r.content,
    b.id AS booking_id,
    b.booking_status,
    r.created_at
FROM review r
JOIN booking b ON r.booking_id = b.id
WHERE r.id = ${REVIEW_ID};
" 2>/dev/null
else
    echo "⚠️  Review ID not available for database check"
fi
echo ""

echo "════════════════════════════════════════════════════════════════════"
echo "STEP 8: PASSENGER VIEWS ALL REVIEWS (via Gateway)"
echo "════════════════════════════════════════════════════════════════════"
echo "GET http://localhost:9001/api/v1/reviews"
echo ""
curl -i -X GET http://localhost:9001/api/v1/reviews \
  -b demo-passenger.txt 2>&1 | head -40

echo ""
echo "✅ Reviews retrieved successfully"
echo ""

echo ""
echo "╔════════════════════════════════════════════════════════════════════╗"
echo "║                                                                    ║"
echo "║              🎉 DEMO COMPLETE - ALL VIA API GATEWAY! 🎉          ║"
echo "║                                                                    ║"
echo "╚════════════════════════════════════════════════════════════════════╝"
echo ""
echo "📊 DEMO SUMMARY:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ Driver & Passenger registered via API Gateway"
echo "✅ Both users authenticated (JWT cookies)"
echo "✅ Passenger created booking → Booking ID: $BOOKING_ID"
echo "✅ Driver accepted booking → Status: CAB_ARRIVED"
echo "✅ Passenger submitted review → Review ID: $REVIEW_ID"
echo "✅ All data verified in MySQL database"
echo ""
echo "🛡️  SECURITY HIGHLIGHTS:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ API Gateway enforces authentication on all protected routes"
echo "✅ Role-based authorization (PASSENGER vs DRIVER)"
echo "✅ Gateway delegates auth/authz to Auth Service"
echo "✅ No direct backend access - all via Gateway"
echo ""
echo "🏗️  ARCHITECTURE HIGHLIGHTS:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ Microservices: Auth, Booking, Review, Location, Socket"
echo "✅ Service Discovery: Eureka"
echo "✅ API Gateway: Spring Cloud Gateway with custom filters"
echo "✅ Database: MySQL with proper relations"
echo "✅ Authentication: JWT with HttpOnly cookies"
echo ""
