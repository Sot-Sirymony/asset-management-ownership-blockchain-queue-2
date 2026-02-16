#!/bin/bash

# Script to start API and Frontend (skips blockchain if Docker unavailable)
# Run from the root directory: ./start-api-frontend.sh

set -e

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
API_DIR="$ROOT_DIR/ownership-api-master"
UI_DIR="$ROOT_DIR/ownership-ui-master"

echo "🚀 Starting API and Frontend..."
echo "================================"

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

# Step 1: Start API Backend
echo ""
echo "🔧 Step 1: Starting API Backend..."
echo "-----------------------------------"
cd "$API_DIR"

# Check port 8081
if lsof -Pi :8081 -sTCP:LISTEN -t >/dev/null 2>&1 ; then
    echo -e "${YELLOW}⚠️  API already running on port 8081${NC}"
else
    # Set environment variable if not set
    if [ -z "$SPRING_DATASOURCE_PASSWORD" ]; then
        export SPRING_DATASOURCE_PASSWORD=postgres
        echo -e "${YELLOW}⚠️  Using default password. Set SPRING_DATASOURCE_PASSWORD if needed.${NC}"
    fi
    
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

# Step 2: Start Frontend
echo ""
echo "🎨 Step 2: Starting Frontend..."
echo "--------------------------------"
cd "$UI_DIR"

# Check port 3000
if lsof -Pi :3000 -sTCP:LISTEN -t >/dev/null 2>&1 ; then
    echo -e "${YELLOW}⚠️  Frontend already running on port 3000${NC}"
else
    # Check if node_modules exists
    if [ ! -d "node_modules" ]; then
        echo "Installing dependencies..."
        npm install || {
            echo -e "${RED}❌ Failed to install frontend dependencies${NC}"
            exit 1
        }
    fi
    
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
echo -e "${GREEN}🎉 API and Frontend Started!${NC}"
echo "================================"
echo ""
echo "Services:"
echo "  🔧 API Backend:        http://localhost:8081"
echo "  📚 Swagger UI:          http://localhost:8081/swagger-ui.html"
echo "  🎨 Frontend:            http://localhost:3000"
echo ""
echo "Note: Blockchain network not started (Docker required)"
echo "      To start blockchain: cd ownership-network-master && ./net.sh up"
echo ""
echo "Logs:"
echo "  API:      tail -f $API_DIR/api.log"
echo "  Frontend: tail -f $UI_DIR/ui.log"
echo ""
