#!/bin/bash

# Script to start all projects: Blockchain Network → API → Frontend
# Run from the root directory: ./start-all-projects.sh

set -e

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
NETWORK_DIR="$ROOT_DIR/ownership-network-master"
API_DIR="$ROOT_DIR/ownership-api-master"
UI_DIR="$ROOT_DIR/ownership-ui-master"

echo "🚀 Starting All Projects..."
echo "================================"

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Function to check if port is in use
check_port() {
    if lsof -Pi :$1 -sTCP:LISTEN -t >/dev/null 2>&1 ; then
        echo -e "${YELLOW}⚠️  Port $1 is already in use${NC}"
        return 1
    else
        echo -e "${GREEN}✅ Port $1 is available${NC}"
        return 0
    fi
}

# Step 1: Start Blockchain Network
echo ""
echo "📦 Step 1: Starting Blockchain Network..."
echo "----------------------------------------"
cd "$NETWORK_DIR"

if [ ! -f "net.sh" ]; then
    echo -e "${RED}❌ net.sh not found in $NETWORK_DIR${NC}"
    exit 1
fi

chmod +x net.sh

# Check if network is already running
if docker ps | grep -q "peer0.org1.ownify.com\|orderer.ownify.com"; then
    echo -e "${YELLOW}⚠️  Blockchain network containers already running${NC}"
    echo "   Run './net.sh status' to check status"
else
    echo "Starting blockchain network (this may take a few minutes)..."
    ./net.sh up || {
        echo -e "${RED}❌ Failed to start blockchain network${NC}"
        echo "   Check logs: ./net.sh logs"
        exit 1
    }
fi

echo -e "${GREEN}✅ Blockchain network started${NC}"
sleep 5

# Step 2: Start API Backend
echo ""
echo "🔧 Step 2: Starting API Backend..."
echo "-----------------------------------"
cd "$API_DIR"

# Check port 8081
check_port 8081

# Set environment variable if not set
if [ -z "$SPRING_DATASOURCE_PASSWORD" ]; then
    export SPRING_DATASOURCE_PASSWORD=postgres
    echo -e "${YELLOW}⚠️  Using default password. Set SPRING_DATASOURCE_PASSWORD if needed.${NC}"
fi

# Check if already running
if lsof -Pi :8081 -sTCP:LISTEN -t >/dev/null 2>&1 ; then
    echo -e "${YELLOW}⚠️  API already running on port 8081${NC}"
else
    echo "Building API..."
    mvn clean compile -DskipTests > /dev/null 2>&1 || {
        echo -e "${RED}❌ API build failed${NC}"
        exit 1
    }
    
    echo "Starting API backend..."
    mvn spring-boot:run > api.log 2>&1 &
    API_PID=$!
    echo "API PID: $API_PID"
    
    # Wait for API to start
    echo "Waiting for API to start..."
    for i in {1..30}; do
        if curl -s http://localhost:8081/swagger-ui.html > /dev/null 2>&1; then
            echo -e "${GREEN}✅ API started successfully${NC}"
            break
        fi
        sleep 2
        if [ $i -eq 30 ]; then
            echo -e "${RED}❌ API failed to start within 60 seconds${NC}"
            echo "   Check api.log for errors"
            exit 1
        fi
    done
fi

# Step 3: Start Frontend
echo ""
echo "🎨 Step 3: Starting Frontend..."
echo "--------------------------------"
cd "$UI_DIR"

# Check port 3000
check_port 3000

# Check if node_modules exists
if [ ! -d "node_modules" ]; then
    echo "Installing dependencies..."
    npm install || {
        echo -e "${RED}❌ Failed to install frontend dependencies${NC}"
        exit 1
    }
fi

# Check if already running
if lsof -Pi :3000 -sTCP:LISTEN -t >/dev/null 2>&1 ; then
    echo -e "${YELLOW}⚠️  Frontend already running on port 3000${NC}"
else
    echo "Starting frontend..."
    npm run dev > ui.log 2>&1 &
    UI_PID=$!
    echo "Frontend PID: $UI_PID"
    
    # Wait for frontend to start
    echo "Waiting for frontend to start..."
    sleep 10
    if curl -s http://localhost:3000 > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Frontend started successfully${NC}"
    else
        echo -e "${YELLOW}⚠️  Frontend may still be starting...${NC}"
    fi
fi

# Summary
echo ""
echo "================================"
echo -e "${GREEN}🎉 All Projects Started!${NC}"
echo "================================"
echo ""
echo "Services:"
echo "  📦 Blockchain Network: Running"
echo "  🔧 API Backend:        http://localhost:8081"
echo "  📚 Swagger UI:          http://localhost:8081/swagger-ui.html"
echo "  🎨 Frontend:            http://localhost:3000"
echo ""
echo "Logs:"
echo "  API:      tail -f $API_DIR/api.log"
echo "  Frontend: tail -f $UI_DIR/ui.log"
echo "  Network:  cd $NETWORK_DIR && ./net.sh logs"
echo ""
echo "To stop all services:"
echo "  ./stop-all-projects.sh"
echo ""
