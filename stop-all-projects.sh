#!/bin/bash

# Script to stop all running projects

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "🛑 Stopping All Projects..."
echo "================================"

# Stop Frontend (port 3000)
if lsof -Pi :3000 -sTCP:LISTEN -t >/dev/null 2>&1 ; then
    echo "Stopping Frontend..."
    lsof -ti:3000 | xargs kill -9 2>/dev/null || true
    echo "✅ Frontend stopped"
fi

# Stop API (port 8081)
if lsof -Pi :8081 -sTCP:LISTEN -t >/dev/null 2>&1 ; then
    echo "Stopping API..."
    lsof -ti:8081 | xargs kill -9 2>/dev/null || true
    echo "✅ API stopped"
fi

# Stop ownership-api compose services (API/PostgreSQL/OTel)
if [ -f "$ROOT_DIR/ownership-api-master/docker-compose.yml" ]; then
    echo "Stopping ownership-api docker services..."
    docker compose -f "$ROOT_DIR/ownership-api-master/docker-compose.yml" stop api postgres otel-collector >/dev/null 2>&1 || true
    echo "✅ ownership-api docker services stopped"
fi

# Stop Blockchain Network
cd "$ROOT_DIR/ownership-network-master"
if [ -f "net.sh" ]; then
    echo "Stopping Blockchain Network..."
    ./net.sh down 2>/dev/null || true
    echo "✅ Blockchain network stopped"
fi

echo ""
echo "✅ All projects stopped"
