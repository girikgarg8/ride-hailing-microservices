# 🚗 Uber WebSocket Demo Client

A simple HTML/JavaScript client to demonstrate and test the WebSocket functionality of the **Uber Client Socket Service**.

## 📋 Overview

This demo client connects to the WebSocket server and demonstrates:
- **Real-time bidirectional communication** using STOMP over WebSocket
- **Publishing messages** to `/app/ping`
- **Subscribing to broadcasts** from `/topic/ping`
- **Connection status monitoring**
- **Message logging and visualization**

## 🎯 Purpose

This client tests the `TestController` in the Uber Client Socket Service:
- **Endpoint**: `ws://localhost:8080/ws`
- **Send to**: `/app/ping` 
- **Receive from**: `/topic/ping`

## 🚀 Quick Start

### Prerequisites

1. **Start the WebSocket Server**:
   ```bash
   cd ../Uber-Client-Socket-Service
   ./gradlew bootRun
   ```
   Server should be running on `http://localhost:8080`

2. **Verify Server is Running**:
   ```bash
   curl http://localhost:8080/actuator/health
   # or check if the port is listening
   lsof -i :8080
   ```

### Run the Demo Client

**Option 1: Using Python HTTP Server** (Recommended)
```bash
# In the Uber-WebSocket-Demo-Client directory
python3 -m http.server 3000
```
Then open: http://localhost:3000

**Option 2: Using Node.js HTTP Server**
```bash
# Install http-server globally (one time)
npm install -g http-server

# Run the server
http-server -p 3000
```
Then open: http://localhost:3000

**Option 3: Open Directly in Browser**
```bash
# macOS
open index.html

# Linux
xdg-open index.html

# Or simply double-click index.html
```

## 📖 How to Use

### 1. **Connect to Server**
- Click the **"Connect"** button
- Wait for the connection status to turn green: ✅ **Connected**

### 2. **Send a Message**
- Enter text in the input field (e.g., "Hello WebSocket!")
- Click **"Send"** or press **Enter**
- Your message will be sent to `/app/ping`

### 3. **Receive Response**
- The server processes your message through `TestController.pingCheck()`
- Server broadcasts response to `/topic/ping`
- All connected clients receive: `"Received: Hello WebSocket!"`

### 4. **Disconnect**
- Click the **"Disconnect"** button to close the WebSocket connection

## 🔄 Message Flow

```
1. Client connects to ws://localhost:8080/ws
   ↓
2. Client subscribes to /topic/ping
   ↓
3. Client sends message to /app/ping with payload: { data: "Hello" }
   ↓
4. Server receives in TestController.pingCheck(TestRequest)
   ↓
5. Server processes and broadcasts to /topic/ping
   ↓
6. Client receives TestResponse: { data: "Received: Hello" }
   ↓
7. Message appears in the log with timestamp
```

## 📁 Project Structure

```
Uber-WebSocket-Demo-Client/
├── index.html           # Main HTML page with WebSocket client
├── assets/
│   └── style.css        # Styling for the demo UI
└── README.md            # This file
```

## 🛠️ Technologies Used

- **SockJS**: WebSocket emulation with fallback options
- **STOMP.js**: Simple Text Oriented Messaging Protocol over WebSocket
- **Vanilla JavaScript**: No frameworks, just plain JS
- **Modern CSS**: Responsive design with animations

## 🎨 Features

### UI Features
✅ Real-time connection status indicator  
✅ Color-coded message logging (sent/received/error/info)  
✅ Automatic scrolling message log  
✅ Responsive design (mobile-friendly)  
✅ Smooth animations and transitions  
✅ Enter key support for sending messages

### Technical Features
✅ SockJS fallback for older browsers  
✅ Automatic reconnection handling  
✅ Error logging and display  
✅ Connection state management  
✅ STOMP protocol over WebSocket  

## 🧪 Testing Scenarios

### Test 1: Basic Ping-Pong
```
Input: "ping"
Expected: "Received: ping"
```

### Test 2: Long Message
```
Input: "This is a longer message to test the WebSocket connection!"
Expected: "Received: This is a longer message to test the WebSocket connection!"
```

### Test 3: Multiple Clients
1. Open the demo in multiple browser tabs
2. Send a message from one tab
3. All tabs should receive the broadcasted response

### Test 4: Reconnection
1. Stop the WebSocket server
2. Try to send a message (should fail)
3. Restart the server
4. Click "Connect" again
5. Should reconnect successfully

## 🔧 Configuration

To change the WebSocket URL, edit `index.html`:

```javascript
const WS_URL = 'http://localhost:8080/ws';  // Change this if your server runs on a different port
```

## 📝 Server Configuration

The demo expects the server to:
- Run on `http://localhost:8080`
- Expose WebSocket endpoint at `/ws`
- Accept messages at `/app/ping`
- Broadcast responses to `/topic/ping`

## 🐛 Troubleshooting

### Connection Fails
```
❌ Problem: "Connection error" message appears
✅ Solution: 
   1. Ensure WebSocket server is running: ./gradlew bootRun
   2. Check server is on port 8080: lsof -i :8080
   3. Verify CORS settings if running on different domains
```

### CORS Issues
```
❌ Problem: Browser blocks connection due to CORS
✅ Solution: 
   Add CORS configuration to WebSocketConfig.java:
   
   registry.addEndpoint("/ws")
         .setAllowedOrigins("http://localhost:3000")
         .withSockJS();
```

### No Response Received
```
❌ Problem: Message sent but no response
✅ Solution:
   1. Check browser console for errors
   2. Verify subscription to /topic/ping is active
   3. Check server logs for message processing
```

### Port Already in Use
```
❌ Problem: Cannot start demo server on port 3000
✅ Solution:
   python3 -m http.server 3001  # Use a different port
```

## 📚 Related Documentation

- [STOMP Protocol Specification](https://stomp.github.io/)
- [SockJS Documentation](https://github.com/sockjs/sockjs-client)
- [Spring WebSocket Guide](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#websocket)

## 🔗 Integration with Other Services

This demo client can be extended to test:
- **Real-time driver location updates**
- **Booking request broadcasts**
- **Ride status notifications**
- **Chat functionality between drivers and passengers**

## 🎯 Next Steps

1. **Extend for Driver Location**: Subscribe to `/topic/drivers/{driverId}`
2. **Add Authentication**: Include JWT tokens in WebSocket headers
3. **Add Booking UI**: Create forms for booking requests
4. **Multiple Subscriptions**: Subscribe to multiple topics simultaneously

## 💡 Tips

- Open browser DevTools (F12) → Console to see detailed logs
- Use Network tab to inspect WebSocket frames
- Test with multiple browser windows to simulate multiple clients
- Keep the message log visible to track all interactions

---

**Ready to test?** Start the server, open the demo, and click Connect! 🚀

