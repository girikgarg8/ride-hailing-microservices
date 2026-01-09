# 🚗 Uber Driver WebSocket Client

A minimalistic WebSocket client for drivers to receive real-time ride requests from the Uber Booking Service.

## 🎯 Purpose

This client allows drivers to:
- **Go Online/Offline** - Connect to the WebSocket server
- **Receive Ride Requests** - Get notified of new bookings in real-time
- **Accept/Reject Rides** - Respond to ride requests
- **View Request Details** - See pickup/dropoff locations, passenger info, etc.

## 🚀 How to Run

### 1. Start the WebSocket Server (Socket Service)
```bash
cd ../Uber-Socket-Service
./gradlew bootRun
# Server should be running on http://localhost:8080
```

### 2. Open the Driver Client
Simply open `index.html` in your web browser:
```bash
open index.html
# Or just double-click the file
```

### 3. Connect as a Driver
1. Enter your Driver ID (e.g., `driver1`, `driver2`)
2. Click **"Go Online"**
3. You're now connected and will receive ride requests!

## 📡 WebSocket Integration

### Connection
- **Endpoint**: `ws://localhost:8080/ws?userId={driverId}`
- **Protocol**: STOMP over SockJS

### Subscriptions
- **Ride Requests**: `/topic/rideRequest`
  - Receives new ride requests broadcast by Booking Service

### Message Format
Expected ride request format:
```json
{
  "bookingId": 11,
  "passengerId": 1,
  "startLatitude": 28.7041,
  "startLongitude": 77.1025,
  "endLatitude": 28.5355,
  "endLongitude": 77.3910,
  "bookingStatus": "ASSIGNING_DRIVER"
}
```

## 🔧 Testing

### Test with Multiple Drivers
1. Open `index.html` in multiple browser tabs
2. Connect each tab with different driver IDs (`driver1`, `driver2`, etc.)
3. Create a booking via Booking Service
4. All connected drivers should receive the ride request

### Test Flow
```bash
# Terminal 1: Start Socket Service
cd Uber-Socket-Service && ./gradlew bootRun

# Terminal 2: Start Booking Service
cd Uber-Booking-Service && ./gradlew bootRun

# Browser: Open Driver Client
open index.html

# Terminal 3: Create a booking
curl -X POST http://localhost:7475/api/v1/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "passengerId": 1,
    "startLocation": {"latitude": 28.7041, "longitude": 77.1025},
    "endLocation": {"latitude": 28.5355, "longitude": 77.3910}
  }'
```

## ✨ Features

- ✅ **Real-time Notifications** - Instant ride request alerts
- ✅ **Clean UI** - Minimalistic design focused on functionality
- ✅ **Visual Feedback** - Color-coded status indicators
- ✅ **Animations** - Smooth slide-in for new requests
- ✅ **Connection Status** - Clear online/offline indicator
- 🔄 **Accept/Reject** - UI ready (backend integration pending)

## 🔮 Next Steps

1. **Integrate Accept/Reject with Booking Service**
   - Send PATCH request to `/api/v1/bookings/{bookingId}`
   - Update booking status and assign driver

2. **Add Driver Authentication**
   - Validate driver credentials
   - Check driver availability status

3. **Enhanced Notifications**
   - Sound alerts for new requests
   - Browser notifications (Web Notifications API)

4. **Driver Location Tracking**
   - Send driver's current location to Location Service
   - Update Redis with driver position

5. **Ride Details Modal**
   - Show passenger details
   - Display route on map
   - Estimated earnings

## 📚 Related Services

- **Uber-Socket-Service**: WebSocket server (port 8080)
- **Uber-Booking-Service**: Creates bookings and broadcasts requests (port 7475)
- **Uber-Location-Service**: Manages driver locations (port 7477)

## 🐛 Troubleshooting

**Connection Failed?**
- Ensure Socket Service is running on port 8080
- Check browser console for error messages
- Verify CORS settings in Socket Service

**Not Receiving Requests?**
- Verify you're subscribed to `/topic/rideRequest`
- Check Socket Service logs for broadcast messages
- Ensure Booking Service is publishing to the correct topic

---

**Built for Module 051**: Integrating Different Microservices For Uber Project

