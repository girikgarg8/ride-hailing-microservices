# 🧪 Socket Service Testing Guide

## 📋 Prerequisites

Ensure these services are running:
- ✅ **Eureka Server** (port 8761)
- ✅ **Location Service** (port 7477)  
- ✅ **Booking Service** (port 7475)
- ✅ **Socket Service** (port 8080) ← We'll start this now

---

## 🚀 Step 1: Start Socket Service

### Option A: Using the startup script
```bash
cd "/Users/ggarg1/Personal/Backend Projects/Microservices-Based Ride-Hailing Platform/Uber-Socket-Service"
./start-service.sh
```

### Option B: Manual start
```bash
cd "/Users/ggarg1/Personal/Backend Projects/Microservices-Based Ride-Hailing Platform/Uber-Socket-Service"
export JAVA_HOME=/Library/Java/JavaVirtualMachines/amazon-corretto-17.jdk/Contents/Home
./gradlew bootRun
```

**Wait for this message:**
```
Started UberClientSocketServiceApplication in X.XXX seconds
Tomcat started on port 8080
```

---

## 🚗 Step 2: Open Driver Client

Open the driver WebSocket client in your browser:
```bash
open "/Users/ggarg1/Personal/Backend Projects/Microservices-Based Ride-Hailing Platform/Uber-Driver-WebSocket-Client/index.html"
```

Or manually navigate to the file in Finder and double-click.

---

## 📱 Step 3: Connect Driver to WebSocket

In the Driver Client browser window:

1. **Enter Driver ID**: `driver1` (or `driver2`, `driver3`, etc.)
2. **Click "Go Online"**
3. **You should see**:
   - Status changes to "Online - driver1" (green)
   - Console log: "✅ Connected as driver: driver1"
   - Alert: "✅ You're now online and ready to receive ride requests!"

---

## 🧪 Step 4: Test End-to-End Flow

### Method 1: Via Booking Service API (Recommended)

Open a new terminal and create a booking:

```bash
curl -X POST http://localhost:7475/api/v1/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "passengerId": 1,
    "startLocation": {"latitude": 28.7041, "longitude": 77.1025},
    "endLocation": {"latitude": 28.5355, "longitude": 77.3910}
  }'
```

**Expected Response:**
```json
{
  "bookingId": 12,
  "bookingStatus": "ASSIGNING_DRIVER",
  "driver": null
}
```

### Method 2: Directly via Socket Service (for testing)

Test the WebSocket broadcast directly:

```bash
curl -X POST http://localhost:8080/api/socket/newride \
  -H "Content-Type: application/json" \
  -d '{
    "bookingId": 999,
    "passengerId": 1,
    "startLatitude": 28.7041,
    "startLongitude": 77.1025,
    "endLatitude": 28.5355,
    "endLongitude": 77.3910,
    "bookingStatus": "ASSIGNING_DRIVER"
  }'
```

---

## ✅ Step 5: Verify Ride Request Received

In the Driver Client browser window, you should see:

1. **🚨 New ride request card appears** with:
   - Booking ID
   - Passenger ID
   - Pickup coordinates
   - Dropoff coordinates
   - Status: ASSIGNING_DRIVER

2. **Two action buttons**:
   - ✅ Accept Ride
   - ❌ Reject

3. **Browser console logs**:
   ```
   🚨 New ride request received: {...}
   ```

---

## 🎯 Step 6: Test Driver Actions

### Accept a Ride
1. Click "✅ Accept Ride" button
2. Card should turn green
3. Status changes to "✅ Accepted - Ride #12"
4. Alert: "✅ Ride #12 accepted!"

### Reject a Ride
1. Click "❌ Reject" button
2. Card opacity fades
3. Shows "Rejected" message

---

## 🔍 Verification Checklist

### Socket Service Logs
```
📞 Received ride request for booking ID: 12
🚨 Broadcasting ride request to all drivers:
   Booking ID: 12
   Passenger ID: 1
   Pickup: (28.7041, 77.1025)
   Dropoff: (28.5355, 77.3910)
   Status: ASSIGNING_DRIVER
✅ Ride request broadcast complete
```

### Browser Console (Driver Client)
```
✅ Connected as driver: driver1
🎧 Subscribed to /topic/rideRequest
🚨 New ride request received: {bookingId: 12, ...}
```

### Network Tab (Browser DevTools)
- WebSocket connection to `ws://localhost:8080/ws`
- STOMP frame received with ride request data

---

## 🧪 Advanced Testing

### Test Multiple Drivers
1. Open driver client in **3 different browser tabs**
2. Connect as `driver1`, `driver2`, `driver3`
3. Create one booking
4. **All 3 drivers** should receive the same ride request
5. First driver to accept gets the ride

### Test Race Condition
1. Two drivers accept the same ride
2. Only first one should succeed (backend validation)
3. Second driver should see error or "already assigned" message

---

## 🐛 Troubleshooting

### Issue: Driver can't connect
- **Check**: Is Socket Service running on port 8080?
  ```bash
  lsof -i:8080
  ```
- **Check**: Browser console for WebSocket errors
- **Fix**: Restart Socket Service

### Issue: No ride requests received
- **Check**: Is driver subscribed to `/topic/rideRequest`?
- **Check**: Socket Service logs for broadcast messages
- **Check**: Browser DevTools → Network → WS tab
- **Fix**: Reconnect driver client

### Issue: Accept button doesn't work
- **Status**: UI placeholder only
- **Next**: Integrate with Booking Service PATCH endpoint
- **Manual test**: 
  ```bash
  curl -X PATCH http://localhost:7475/api/v1/bookings/12 \
    -H "Content-Type: application/json" \
    -d '{"status": "SCHEDULED", "driverId": 2}'
  ```

---

## 📊 Complete Test Flow

```
1. Passenger → Booking Service
   POST /api/v1/bookings
   
2. Booking Service → Location Service  
   Find nearby drivers (via Retrofit + Eureka)
   
3. Booking Service → Socket Service
   POST /api/socket/newride
   
4. Socket Service → WebSocket broadcast
   /topic/rideRequest
   
5. All Connected Drivers receive notification
   
6. Driver clicks "Accept"
   
7. (Future) Driver Client → Booking Service
   PATCH /api/v1/bookings/{id}
   
8. Booking updated with driver assignment
```

---

## 🎯 Next Steps

1. **Integrate Accept/Reject** with Booking Service
2. **Add driver authentication**
3. **Filter by nearby drivers only** (use geospatial from Location Service)
4. **Add notification sounds**
5. **Show driver location on map**

---

**Built for Module 051**: Integrating Different Microservices For Uber Project

