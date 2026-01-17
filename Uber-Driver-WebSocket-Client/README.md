# Uber Driver WebSocket Client

A web-based client for Uber drivers to receive and respond to ride requests in real-time through WebSockets, **fully integrated with API Gateway authentication**.

## 🎯 Features

- **🔐 Secure Authentication**: Driver login via API Gateway with JWT cookies
- **🌐 WebSocket via API Gateway**: All WebSocket connections routed through the API Gateway (port 9001)
- **🔒 Role-Based Access**: Only users with `DRIVER` role can access the portal
- **📡 Real-Time Notifications**: Instant ride request notifications via WebSocket
- **✅ Accept/Reject Rides**: Interactive buttons to respond to ride requests
- **🎨 Modern UI**: Clean, responsive interface with animations

## 🏗️ Architecture

```
Driver Browser
    ↓
    1. HTTP POST /api/v1/auth/signin
    ↓
API Gateway (port 9001) → Auth Service
    ↓ (JWT Cookie set)
    ↓
    2. WebSocket /ws?userId={driverId}
    ↓
API Gateway → Socket Service
    ↓ (WebSocket Proxy)
    ↓
Driver receives ride requests via /topic/rideRequest
```

### Key Security Features:
1. **Authentication**: Driver must login with email/password before connecting
2. **JWT Cookies**: Secure HttpOnly cookies automatically sent with WebSocket handshake
3. **API Gateway Enforcement**: All requests (HTTP + WebSocket) go through the Gateway
4. **Role Validation**: Gateway validates DRIVER role before allowing WebSocket connection

## 🚀 How to Use

### 1. Start All Services
Ensure all microservices are running:
```bash
cd /path/to/Microservices-Based Ride-Hailing Platform
./start-all-services.sh
```

This will start:
- Service Discovery (Eureka) - port 8761
- Auth Service - port 9090
- Socket Service - port 8080
- **API Gateway - port 9001** ⭐

### 2. Create a Driver Account (if you haven't already)

**Option A: Via Demo Script**
```bash
./api-gateway-demo.sh
```
This creates a demo driver: `demo-driver@uber.com` / `driver123`

**Option B: Via cURL**
```bash
curl -X POST http://localhost:9001/api/v1/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "your-driver@uber.com",
    "password": "yourpassword",
    "name": "Your Name",
    "phoneNumber": "+1234567890",
    "role": "DRIVER"
  }'
```

### 3. Open the WebSocket Client
```bash
# From the project root
open Uber-Driver-WebSocket-Client/index.html
```

Or simply open `index.html` in your browser.

### 4. Login and Go Online

1. **Enter Credentials**:
   - Email: `demo-driver@uber.com`
   - Password: `driver123`
   - Click **"Login"**

2. **Go Online**:
   - After successful login, click **"Go Online"**
   - You're now connected via WebSocket and ready to receive ride requests!

### 5. Receive and Respond to Ride Requests

When a passenger creates a booking, you'll see:
- 🚨 **New Ride Request** notification with:
  - Booking ID
  - Passenger ID
  - Pickup location (latitude, longitude)
  - Dropoff location (latitude, longitude)
  - Current status

**Actions**:
- ✅ **Accept Ride**: Updates booking status and assigns you as the driver
- ❌ **Reject**: Declines the ride request

## 🔧 Technical Details

### Connection Flow

1. **Login (HTTP)**:
   ```javascript
   POST http://localhost:9001/api/v1/auth/signin
   → API Gateway validates and forwards to Auth Service
   → Auth Service returns JWT cookie
   → Browser stores cookie automatically
   ```

2. **WebSocket Connection**:
   ```javascript
   new SockJS("http://localhost:9001/ws?userId={driverId}")
   → Browser sends JWT cookie with WebSocket handshake
   → API Gateway validates cookie and role (DRIVER)
   → Connection proxied to Socket Service
   → WebSocket connection established
   ```

3. **Subscribe to Ride Requests**:
   ```javascript
   stompClient.subscribe("/topic/rideRequest", callback)
   → Listens for new ride requests broadcast by Socket Service
   ```

4. **Send Ride Response**:
   ```javascript
   stompClient.send("/app/rideResponse/{driverId}", {}, JSON.stringify({
     response: true,  // true = accept, false = reject
     bookingId: 123
   }))
   → Socket Service processes response
   → Calls Booking Service to update booking
   → Kafka event published for booking update
   ```

### API Gateway Integration

The WebSocket route in API Gateway (`application.yml`):
```yaml
- id: socket-service-ws
  uri: lb://UBER-SOCKET-SERVICE
  predicates:
    - Path=/ws/**
  filters:
    - AuthenticationDelegationFilter
  metadata:
    requiredRole: DRIVER
```

**What this does**:
- ✅ Routes WebSocket connections through load balancer to Socket Service
- ✅ Applies `AuthenticationDelegationFilter` to validate JWT cookie
- ✅ Ensures only authenticated users with `DRIVER` role can connect
- ✅ Proxies WebSocket connection after successful authentication

## 📊 Demo Workflow

### Complete End-to-End Demo:

1. **Start Services** (if not already running):
   ```bash
   ./start-all-services.sh
   ```

2. **Open Driver Client**:
   ```bash
   open Uber-Driver-WebSocket-Client/index.html
   ```

3. **Login as Driver**:
   - Email: `demo-driver@uber.com`
   - Password: `driver123`
   - Click "Go Online"

4. **Create a Booking** (in another terminal):
   ```bash
   ./api-gateway-demo.sh
   ```
   This will:
   - Register passenger
   - Create a booking
   - Broadcast ride request to all online drivers

5. **Accept the Ride** (in Driver Client):
   - You'll see the ride request pop up
   - Click "✅ Accept Ride"
   - Booking status updates to `CAB_ARRIVED`

## 🐛 Troubleshooting

### Issue: Login fails with 401 Unauthorized
**Solution**: 
- Ensure Auth Service is running: `curl http://localhost:9090/actuator/health`
- Verify API Gateway is running: `curl http://localhost:9001/actuator/health`
- Check if driver account exists in the database

### Issue: WebSocket connection fails
**Solution**:
- Ensure you're logged in first (JWT cookie must be set)
- Check Socket Service is running: `curl http://localhost:8080/actuator/health`
- Verify API Gateway routes: `curl http://localhost:9001/actuator/gateway/routes`
- Check browser console for detailed error messages

### Issue: Not receiving ride requests
**Solution**:
- Ensure you're connected (status shows "Online")
- Check that Socket Service is running
- Verify booking was created successfully
- Look at browser console for subscription confirmation

### Issue: "Only drivers can access this portal" error
**Solution**:
- You logged in with a PASSENGER or ADMIN account
- Create a new account with `"role": "DRIVER"` in the signup request

## 🔍 What Makes This Different from Basic WebSocket?

### Before (Direct Connection - ❌):
```
Driver → ws://localhost:8080/ws
- No authentication
- No authorization
- Direct connection to backend
- Bypasses API Gateway
- Not scalable or secure
```

### After (Via API Gateway - ✅):
```
Driver → Login via Gateway → JWT Cookie
      → ws://localhost:9001/ws → Gateway validates → Socket Service
- Authenticated with JWT
- Role-based authorization
- All traffic via Gateway
- Centralized security
- Production-ready architecture
```

## 📝 Resume Talking Points

When discussing this in interviews:

1. **"Built a real-time driver notification system using WebSockets through an API Gateway"**
   - WebSocket connections routed through Spring Cloud Gateway
   - JWT-based authentication for WebSocket handshake
   - Role-based authorization (DRIVER role required)

2. **"Implemented secure WebSocket authentication using JWT cookies"**
   - Browser automatically sends HttpOnly cookies with WebSocket handshake
   - Gateway validates token before proxying connection
   - No token exposure in URL parameters

3. **"Designed a centralized security architecture with API Gateway as the single entry point"**
   - All HTTP and WebSocket traffic routed through Gateway
   - Authentication and authorization enforced at the edge
   - Backend services don't need to handle auth logic

4. **"Built a responsive real-time UI with STOMP over SockJS"**
   - Subscribe to topics for ride requests
   - Send messages for ride acceptance/rejection
   - Graceful degradation and error handling

## 📚 Technologies Used

- **SockJS**: WebSocket emulation with fallback options
- **STOMP**: Simple Text Oriented Messaging Protocol over WebSocket
- **Spring Cloud Gateway**: API Gateway with WebSocket proxy support
- **JWT**: JSON Web Tokens for stateless authentication
- **Vanilla JavaScript**: No framework dependencies
- **Modern CSS**: Animations, flexbox, responsive design

## 🎓 Learning Resources

- [Spring Cloud Gateway WebSocket Support](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#websocket-routing-filter)
- [STOMP Protocol Specification](https://stomp.github.io/)
- [SockJS Documentation](https://github.com/sockjs/sockjs-client)
- [JWT Best Practices](https://tools.ietf.org/html/rfc8725)

---

**Note**: This client is designed for demo and development purposes. For production use, consider:
- HTTPS/WSS instead of HTTP/WS
- More robust error handling
- Connection retry logic
- Token refresh mechanism
- Service worker for offline support
