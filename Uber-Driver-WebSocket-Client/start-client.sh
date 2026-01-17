#!/bin/bash

# Start a simple HTTP server for the Driver WebSocket Client
# This avoids file:// protocol and CORS issues with Origin: null

PORT=3000

echo "╔════════════════════════════════════════════════════════════════╗"
echo "║                                                                ║"
echo "║       🚗 UBER DRIVER WEBSOCKET CLIENT SERVER 🚗              ║"
echo "║                                                                ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""
echo "Starting HTTP server on port $PORT..."
echo ""
echo "📱 Open this URL in your browser:"
echo "   👉 http://localhost:$PORT/index.html"
echo ""
echo "🔑 Demo Login Credentials:"
echo "   Email:    demo-driver@uber.com"
echo "   Password: driver123"
echo ""
echo "⚠️  Press Ctrl+C to stop the server"
echo ""

# Check if Python 3 is available
if command -v python3 &> /dev/null; then
    python3 -m http.server $PORT
elif command -v python &> /dev/null; then
    python -m SimpleHTTPServer $PORT
else
    echo "❌ Error: Python is not installed. Please install Python to run this server."
    exit 1
fi
