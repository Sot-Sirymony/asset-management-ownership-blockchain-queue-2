#!/bin/bash

# Script to stop all running projects

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

# Stop Blockchain Network
cd "$(dirname "$0")/ownership-network-master"
if [ -f "net.sh" ]; then
    echo "Stopping Blockchain Network..."
    ./net.sh down 2>/dev/null || true
    echo "✅ Blockchain network stopped"
fi

echo ""
echo "✅ All projects stopped"
