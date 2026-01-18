#!/bin/bash

# Configure WebSocket Client for AWS Deployment
# This script updates the API Gateway URL in index.html for AWS deployment

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}🔧 Configuring WebSocket Client for AWS Deployment${NC}"
echo "=================================================="

# Check if GATEWAY_PUBLIC_IP is provided
if [ -z "$1" ]; then
    echo -e "${RED}❌ Error: Gateway Public IP not provided${NC}"
    echo ""
    echo "Usage: $0 <GATEWAY_PUBLIC_IP>"
    echo ""
    echo "Example:"
    echo "  $0 54.123.45.67"
    echo ""
    echo "Or set environment variable:"
    echo "  export GATEWAY_PUBLIC_IP=54.123.45.67"
    echo "  $0 \$GATEWAY_PUBLIC_IP"
    exit 1
fi

GATEWAY_PUBLIC_IP=$1

# Validate IP format (basic check)
if [[ ! $GATEWAY_PUBLIC_IP =~ ^[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}$ ]]; then
    echo -e "${RED}❌ Error: Invalid IP address format: $GATEWAY_PUBLIC_IP${NC}"
    echo "Please provide a valid IPv4 address (e.g., 54.123.45.67)"
    exit 1
fi

# Check if index.html exists
if [ ! -f "index.html" ]; then
    echo -e "${RED}❌ Error: index.html not found in current directory${NC}"
    echo "Please run this script from the Uber-Driver-WebSocket-Client directory"
    exit 1
fi

# Create backup
echo -e "${YELLOW}📋 Creating backup of index.html...${NC}"
cp index.html index.html.backup
echo "✅ Backup created: index.html.backup"

# Update the API Gateway URL
echo -e "${YELLOW}🔄 Updating API Gateway URL...${NC}"
sed -i.tmp "s/GATEWAY_PUBLIC_IP/$GATEWAY_PUBLIC_IP/g" index.html

# Clean up temporary file
rm -f index.html.tmp

# Verify the change
if grep -q "http://$GATEWAY_PUBLIC_IP:9001" index.html; then
    echo -e "${GREEN}✅ Successfully updated API Gateway URL to: http://$GATEWAY_PUBLIC_IP:9001${NC}"
else
    echo -e "${RED}❌ Error: Failed to update API Gateway URL${NC}"
    echo "Restoring backup..."
    mv index.html.backup index.html
    exit 1
fi

echo ""
echo -e "${GREEN}🎉 Configuration Complete!${NC}"
echo "=================================================="
echo -e "📝 ${YELLOW}Next Steps:${NC}"
echo "1. Upload to AWS Client instance:"
echo "   scp -i uber-platform-key.pem -r . ec2-user@<CLIENT_PUBLIC_IP>:~/Uber-Driver-WebSocket-Client/"
echo ""
echo "2. SSH to Client instance and start server:"
echo "   ssh -i uber-platform-key.pem ec2-user@<CLIENT_PUBLIC_IP>"
echo "   cd ~/Uber-Driver-WebSocket-Client"
echo "   ./start-client.sh"
echo ""
echo "3. Access client in browser:"
echo "   http://<CLIENT_PUBLIC_IP>:3000/index.html"
echo ""
echo -e "${YELLOW}💡 Tip:${NC} To revert changes, restore from backup:"
echo "   mv index.html.backup index.html"