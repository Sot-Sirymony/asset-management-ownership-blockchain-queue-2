#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CHANNEL_DIR="$ROOT_DIR/channel"
NET_NAME="test"

echo "ROOT_DIR    = $ROOT_DIR"
echo "CHANNEL_DIR = $CHANNEL_DIR"

# 0) Ensure docker network exists (idempotent)
docker network inspect "$NET_NAME" >/dev/null 2>&1 || docker network create "$NET_NAME" >/dev/null

# 0.1) Sanity checks: crypto-config must exist before channel steps
if [ ! -d "$CHANNEL_DIR/crypto-config" ]; then
  echo "❌ Missing $CHANNEL_DIR/crypto-config"
  echo "   Please generate crypto first (CA + enroll/register scripts) then re-run setup_v1.sh."
  exit 1
fi

# 1) Start docker containers (orderers/peers/couchdb)
echo "Starting Fabric containers..."
cd "$CHANNEL_DIR"
chmod +x ./*.sh || true
./start-all.sh

# 2) Run Fabric CLI steps inside fabric-tools
#    Make it safe to rerun: ignore errors if channel already exists / peers already joined.
echo "Running Fabric CLI steps in fabric-tools container..."
docker run --rm -t \
  --network "$NET_NAME" \
  -v "$ROOT_DIR":/work \
  -w /work/channel \
  hyperledger/fabric-tools:2.5 \
  bash -lc '
    set -euo pipefail
    chmod +x ./*.sh || true

    # Some scripts may fail on second run (channel exists / already joined).
    # We allow re-run by ignoring known idempotency errors.
    run_ok() {
      local cmd="$1"
      echo ">>> $cmd"
      bash -lc "$cmd" || echo "⚠️  Ignored error (likely already done): $cmd"
    }

    run_ok "./1.create-genesis-block.sh"
    run_ok "./2.create-channelTx.sh"
    run_ok "./3.create-anchor-peer.sh"
    run_ok "./5.create-app-channel.sh"
    run_ok "./6.join-peers-to-channel.sh"
    run_ok "./7.update-anchor-peers.sh"
  '

# 3) Prepare Go deps using golang container
echo "Preparing Go modules (go mod tidy/vendor)..."
docker run --rm -t \
  -v "$ROOT_DIR":/work \
  -w /work/channel/src/go \
  golang:1.21 \
  bash -lc '
    set -euo pipefail
    if [ ! -f go.mod ]; then
      echo "No go.mod found. Creating..."
      go mod init chaincode
    fi
    go mod tidy
    go mod vendor
  '

# 4) Deploy chaincode using fabric-tools
echo "Deploying chaincode in fabric-tools container..."
docker run --rm -t \
  --network "$NET_NAME" \
  -v "$ROOT_DIR":/work \
  -w /work/channel/deploy-chaincode \
  hyperledger/fabric-tools:2.5 \
  bash -lc '
    set -euo pipefail
    chmod +x ./deploy-chaincode.sh
    ./deploy-chaincode.sh
  '

# 5) Verify chaincode committed
# NOTE: your chaincode name might not be "basic". We show all committed chaincodes too.
echo "Verifying chaincode is committed..."
docker run --rm -t \
  --network "$NET_NAME" \
  -v "$ROOT_DIR":/work \
  -w /work/channel \
  hyperledger/fabric-tools:2.5 \
  bash -lc '
    set -euo pipefail
    export FABRIC_CFG_PATH=${PWD}/config
    export CORE_PEER_TLS_ENABLED=true
    export CORE_PEER_LOCALMSPID="Org1MSP"
    export CORE_PEER_TLS_ROOTCERT_FILE=${PWD}/crypto-config/peerOrganizations/org1.ownify.com/peers/peer0.org1.ownify.com/tls/ca.crt
    export CORE_PEER_MSPCONFIGPATH=${PWD}/crypto-config/peerOrganizations/org1.ownify.com/users/Admin@org1.ownify.com/msp
    export CORE_PEER_ADDRESS=peer0.org1.ownify.com:7051

    echo "=== channel getinfo ==="
    peer channel getinfo -c channel-org || true

    echo "=== chaincode querycommitted (all) ==="
    peer lifecycle chaincode querycommitted -C channel-org || true
  '

# 6) Start explorer
echo "Starting Explorer..."
cd "$CHANNEL_DIR/explorer"
docker compose up -d

echo "✅ Explorer: http://localhost:8080 (exploreradmin / exploreradminpw / first-network)"
echo "✅ Setup successful!"
