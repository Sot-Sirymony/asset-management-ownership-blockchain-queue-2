#!/bin/bash

# Script to start all projects: Blockchain Network → API → Frontend
# Run from the root directory: ./start-all-projects.sh

set -e

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
NETWORK_DIR="$ROOT_DIR/ownership-network-master"
API_DIR="$ROOT_DIR/ownership-api-master"
UI_DIR="$ROOT_DIR/ownership-ui-master"
FABRIC_LOCAL_CA_CERT="$NETWORK_DIR/channel/crypto-config/peerOrganizations/org1.ownify.com/users/Admin@org1.ownify.com/tls/ca.crt"
API_HEALTH_URL="http://localhost:8081/v3/api-docs"
API_SWAGGER_URL="http://localhost:8081/swagger-ui/index.html"

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

ensure_local_postgres() {
    local url="${SPRING_DATASOURCE_URL:-}"
    local db_port
    if [[ "$url" == *"localhost:"* ]] || [[ "$url" == *"127.0.0.1:"* ]]; then
        db_port="$(echo "$url" | sed -nE 's#^jdbc:postgresql://(localhost|127\.0\.0\.1):([0-9]+)/.*#\2#p')"
        [ -n "$db_port" ] || db_port="55432"

        if ! lsof -Pi :"$db_port" -sTCP:LISTEN -t >/dev/null 2>&1 ; then
            echo -e "${YELLOW}⚠️  PostgreSQL is not listening on ${db_port}${NC}"
            echo "   Attempting to start PostgreSQL from ownership-api docker-compose..."

            if ! docker info >/dev/null 2>&1 ; then
                echo -e "${RED}❌ Docker daemon is not available${NC}"
                echo "   Start Docker Desktop, or set SPRING_DATASOURCE_URL to a reachable DB host."
                echo "   Current URL: $url"
                exit 1
            fi

            # ownership-api compose uses external network "test"
            docker network inspect test >/dev/null 2>&1 || docker network create test >/dev/null 2>&1 || true

            if ! POSTGRES_HOST_PORT="$db_port" docker compose -f "$API_DIR/docker-compose.yml" up -d postgres ; then
                echo -e "${RED}❌ Failed to start postgres service via docker compose${NC}"
                docker compose -f "$API_DIR/docker-compose.yml" logs --tail=80 postgres 2>/dev/null || true
                exit 1
            fi

            echo "Waiting for PostgreSQL to be ready..."
            for i in {1..30}; do
                if lsof -Pi :"$db_port" -sTCP:LISTEN -t >/dev/null 2>&1 ; then
                    echo -e "${GREEN}✅ PostgreSQL is ready on ${db_port}${NC}"
                    return 0
                fi
                sleep 2
                if [ $i -eq 30 ]; then
                    echo -e "${RED}❌ PostgreSQL did not become ready in time${NC}"
                    docker compose -f "$API_DIR/docker-compose.yml" logs --tail=80 postgres 2>/dev/null || true
                    exit 1
                fi
            done
        fi
    fi
}

bootstrap_local_postgres_schema() {
    local url="${SPRING_DATASOURCE_URL:-}"
    local db_state
    local sql_files=()
    local sql_file
    local apply_failed=0

    if [[ "$url" != *"localhost:"* ]] && [[ "$url" != *"127.0.0.1:"* ]]; then
        return 0
    fi

    if ! docker info >/dev/null 2>&1 ; then
        return 0
    fi

    shopt -s nullglob
    sql_files=("$API_DIR"/db-init/*.sql)
    shopt -u nullglob
    if [ "${#sql_files[@]}" -eq 0 ]; then
        return 0
    fi

    db_state="$(docker inspect -f '{{.State.Running}}' ownership-postgres 2>/dev/null || true)"
    if [ "$db_state" != "true" ]; then
        return 0
    fi

    for sql_file in "${sql_files[@]}"; do
        if docker exec -i ownership-postgres \
            psql -v ON_ERROR_STOP=1 \
            -U "${POSTGRES_USER:-postgres}" \
            -d "${POSTGRES_DB:-asset_holder_db}" < "$sql_file" >/dev/null 2>&1 ; then
            echo -e "${GREEN}✅ Applied $(basename "$sql_file")${NC}"
        else
            apply_failed=1
            echo -e "${YELLOW}⚠️  Could not auto-apply $(basename "$sql_file")${NC}"
        fi
    done

    if [ "$apply_failed" -eq 0 ]; then
        echo -e "${GREEN}✅ Ensured PostgreSQL schema and seed data${NC}"
    else
        echo -e "${YELLOW}⚠️  Some DB bootstrap scripts failed${NC}"
        echo -e "${YELLOW}   You can run manually: for f in ownership-api-master/db-init/*.sql; do docker exec -i ownership-postgres psql -U postgres -d asset_holder_db < \"\$f\"; done${NC}"
    fi
}

fabric_channel_ready() {
    docker ps --format '{{.Names}}' | grep -q '^cli$' || return 1
    docker exec cli peer channel getinfo -c channel-org >/dev/null 2>&1
}

basic_chaincode_ready() {
    docker exec cli peer lifecycle chaincode querycommitted -C channel-org 2>/dev/null | grep -q "Name: basic,"
}

ensure_fabric_channel_and_chaincode() {
    if ! docker info >/dev/null 2>&1; then
        echo -e "${RED}❌ Docker daemon is not available for blockchain checks${NC}"
        exit 1
    fi

    if ! fabric_channel_ready; then
        echo -e "${YELLOW}⚠️  channel-org is not accessible from cli; attempting recovery...${NC}"
        if ! (cd "$NETWORK_DIR" && ./net.sh channel); then
            echo -e "${RED}❌ Unable to recover channel-org automatically${NC}"
            echo "   Try a clean network reset:"
            echo "   cd ownership-network-master && ./net.sh reset && ./net.sh up"
            exit 1
        fi
    fi

    if ! fabric_channel_ready; then
        echo -e "${RED}❌ channel-org is still unavailable after recovery attempt${NC}"
        echo "   Try: cd ownership-network-master && ./net.sh reset && ./net.sh up"
        exit 1
    fi
    echo -e "${GREEN}✅ channel-org is available${NC}"

    if ! basic_chaincode_ready; then
        echo -e "${YELLOW}⚠️  Chaincode 'basic' not committed on channel-org; deploying...${NC}"
        if ! (cd "$NETWORK_DIR" && ./net.sh deploy-cc); then
            echo -e "${RED}❌ Failed to deploy chaincode 'basic'${NC}"
            exit 1
        fi
    fi

    if ! basic_chaincode_ready; then
        echo -e "${RED}❌ Chaincode 'basic' is still not committed on channel-org${NC}"
        exit 1
    fi
    echo -e "${GREEN}✅ Chaincode 'basic' is committed on channel-org${NC}"
}

seed_blockchain_ledger_data() {
    if ! docker info >/dev/null 2>&1; then
        return 0
    fi

    if ! docker ps --format '{{.Names}}' | grep -q '^cli$'; then
        return 0
    fi

    if ! docker exec cli peer channel getinfo -c channel-org >/dev/null 2>&1; then
        echo -e "${YELLOW}⚠️  Skipping blockchain seed: channel-org unavailable${NC}"
        return 0
    fi

    if ! docker exec cli peer lifecycle chaincode querycommitted -C channel-org 2>/dev/null | grep -q "Name: basic,"; then
        echo -e "${YELLOW}⚠️  Skipping blockchain seed: chaincode 'basic' not committed${NC}"
        return 0
    fi

    if docker exec cli bash -lc '
        set -euo pipefail

        export FABRIC_CFG_PATH=/etc/hyperledger/fabric/config
        export CORE_PEER_TLS_ENABLED=true
        export CORE_PEER_LOCALMSPID=Org1MSP
        export CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.ownify.com/users/Admin@org1.ownify.com/msp
        export CORE_PEER_ADDRESS=peer0.org1.ownify.com:7051
        export CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.ownify.com/peers/peer0.org1.ownify.com/tls/ca.crt
        export ORDERER_CA=/etc/hyperledger/fabric/crypto-config/ordererOrganizations/ownify.com/orderers/orderer.ownify.com/tls/ca.crt

        seed_asset_id="AssetSeed001"
        seed_asset_name="Seed Laptop"
        seed_asset_assign_to="1"
        seed_asset_username="admin"
        seed_asset_unit="pcs"
        seed_asset_condition="New"
        seed_asset_attachment="seed-attachment"
        seed_asset_dep="IT"
        seed_asset_qty="1"
        seed_report_id="ReportSeed001"
        seed_report_problem="Initial seeded report for smoke test"
        seed_report_attachment="seed-report-attachment"

        asset_json="$(peer chaincode query -C channel-org -n basic -c "{\"Args\":[\"QueryAsset\",\"${seed_asset_id}\"]}" 2>/dev/null || true)"
        if [ -n "${asset_json}" ]; then
            echo "Seed asset ${seed_asset_id} already exists; skipping."
        else
            echo "Creating seed asset ${seed_asset_id}..."
            peer chaincode invoke -o orderer.ownify.com:7050 \
                --tls --cafile "${ORDERER_CA}" \
                -C channel-org -n basic \
                --peerAddresses peer0.org1.ownify.com:7051 \
                --tlsRootCertFiles "${CORE_PEER_TLS_ROOTCERT_FILE}" \
                --waitForEvent \
                -c "{\"function\":\"CreateAsset\",\"Args\":[\"${seed_asset_id}\",\"${seed_asset_name}\",\"${seed_asset_unit}\",\"${seed_asset_condition}\",\"${seed_asset_attachment}\",\"${seed_asset_assign_to}\",\"${seed_asset_username}\",\"${seed_asset_dep}\",\"${seed_asset_qty}\"]}" >/dev/null
            asset_json="$(peer chaincode query -C channel-org -n basic -c "{\"Args\":[\"QueryAsset\",\"${seed_asset_id}\"]}" 2>/dev/null || true)"
        fi

        if ! echo "${asset_json}" | grep -q "\"assign_to\":\"${seed_asset_assign_to}\""; then
            echo "Normalizing seed asset ${seed_asset_id} assign_to -> ${seed_asset_assign_to}..."
            peer chaincode invoke -o orderer.ownify.com:7050 \
                --tls --cafile "${ORDERER_CA}" \
                -C channel-org -n basic \
                --peerAddresses peer0.org1.ownify.com:7051 \
                --tlsRootCertFiles "${CORE_PEER_TLS_ROOTCERT_FILE}" \
                --waitForEvent \
                -c "{\"function\":\"UpdateAsset\",\"Args\":[\"${seed_asset_id}\",\"${seed_asset_name}\",\"${seed_asset_unit}\",\"${seed_asset_condition}\",\"${seed_asset_attachment}\",\"${seed_asset_assign_to}\",\"${seed_asset_username}\",\"${seed_asset_dep}\",\"${seed_asset_qty}\"]}" >/dev/null
        fi

        if peer chaincode query -C channel-org -n basic -c "{\"Args\":[\"QueryReportIssue\",\"${seed_report_id}\"]}" >/dev/null 2>&1; then
            echo "Seed report ${seed_report_id} already exists; skipping."
        else
            echo "Creating seed report ${seed_report_id}..."
            peer chaincode invoke -o orderer.ownify.com:7050 \
                --tls --cafile "${ORDERER_CA}" \
                -C channel-org -n basic \
                --peerAddresses peer0.org1.ownify.com:7051 \
                --tlsRootCertFiles "${CORE_PEER_TLS_ROOTCERT_FILE}" \
                --waitForEvent \
                -c "{\"function\":\"CreateReportIssue\",\"Args\":[\"${seed_report_id}\",\"${seed_asset_id}\",\"${seed_asset_name}\",\"${seed_report_problem}\",\"${seed_report_attachment}\",\"${seed_asset_assign_to}\",\"${seed_asset_username}\"]}" >/dev/null
        fi
    '; then
        echo -e "${GREEN}✅ Ensured blockchain seed data (asset/report)${NC}"
    else
        echo -e "${YELLOW}⚠️  Blockchain seed step failed (startup will continue)${NC}"
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
ensure_fabric_channel_and_chaincode
seed_blockchain_ledger_data

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

if [ -z "${SPRING_DATASOURCE_USERNAME:-}" ]; then
    export SPRING_DATASOURCE_USERNAME=postgres
fi

# Local Maven run should use localhost PostgreSQL, not host.docker.internal.
if [ -z "${SPRING_DATASOURCE_URL:-}" ]; then
    export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:55432/asset_holder_db
    echo -e "${GREEN}✅ Using default local DB URL: $SPRING_DATASOURCE_URL${NC}"
fi
ensure_local_postgres
bootstrap_local_postgres_schema

# Use local Fabric CA certificate path when running API outside Docker
if [ -z "${FABRIC_CA_PEM_FILE:-}" ]; then
    if [ -f "$FABRIC_LOCAL_CA_CERT" ]; then
        export FABRIC_CA_PEM_FILE="$FABRIC_LOCAL_CA_CERT"
        echo -e "${GREEN}✅ Using local FABRIC_CA_PEM_FILE: $FABRIC_CA_PEM_FILE${NC}"
    else
        echo -e "${YELLOW}⚠️  Local Fabric CA cert not found at:$FABRIC_LOCAL_CA_CERT${NC}"
        echo -e "${YELLOW}   API may fail if FABRIC_CA_PEM_FILE is not configured.${NC}"
    fi
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
        if curl -s "$API_HEALTH_URL" > /dev/null 2>&1; then
            # Ensure listener remains stable for a short moment
            sleep 2
            if lsof -Pi :8081 -sTCP:LISTEN -t >/dev/null 2>&1 ; then
                echo -e "${GREEN}✅ API started successfully${NC}"
                break
            fi
        fi
        if ! kill -0 "$API_PID" >/dev/null 2>&1 ; then
            echo -e "${RED}❌ API process exited during startup${NC}"
            echo "   Check api.log for errors"
            exit 1
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
echo "  📚 Swagger UI:          $API_SWAGGER_URL"
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
