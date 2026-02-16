#!/bin/bash

# Quick status check script for blockchain network

echo "📊 Blockchain Network Status Check"
echo "===================================="
echo ""

# Check Docker
echo "1. Checking Docker..."
if docker ps >/dev/null 2>&1; then
    echo "   ✅ Docker is accessible"
else
    echo "   ❌ Docker permission denied - Start Docker Desktop!"
    echo "   → Open Docker Desktop application"
    echo "   → Wait for it to fully start"
    exit 1
fi

echo ""
echo "2. Current Running Containers:"
echo "-------------------------------"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" 2>/dev/null | head -20

echo ""
echo "3. Blockchain Network Containers:"
echo "----------------------------------"
BLOCKCHAIN_CONTAINERS=$(docker ps --format "{{.Names}}" 2>/dev/null | grep -E "peer|orderer|ca|couchdb|explorer" || echo "None")

if [ "$BLOCKCHAIN_CONTAINERS" = "None" ]; then
    echo "   ⚠️  No blockchain containers running"
    echo ""
    echo "   To start the network, run:"
    echo "   ./net.sh up"
else
    echo "   ✅ Blockchain containers found:"
    echo "$BLOCKCHAIN_CONTAINERS" | sed 's/^/      - /'
fi

echo ""
echo "4. Checking for stopped containers:"
echo "-------------------------------------"
STOPPED=$(docker ps -a --format "{{.Names}}" 2>/dev/null | grep -E "peer|orderer|ca|couchdb|explorer" | wc -l)
if [ "$STOPPED" -gt 0 ]; then
    echo "   ⚠️  Found $STOPPED stopped blockchain containers"
    echo "   Run: docker ps -a | grep -E 'peer|orderer|ca|couchdb'"
else
    echo "   ✅ No stopped blockchain containers"
fi

echo ""
echo "===================================="
echo "Quick Commands:"
echo "  ./net.sh status    - Check status"
echo "  ./net.sh up        - Start network"
echo "  ./net.sh down      - Stop network"
echo "  ./net.sh logs      - View logs"
echo "===================================="
