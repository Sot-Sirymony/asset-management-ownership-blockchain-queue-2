#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
echo "ROOT_DIR = $ROOT_DIR"

# 1) Start docker containers (orderers/peers/couchdb) using your existing script
echo "Starting Fabric containers..."
cd "$ROOT_DIR/channel"
./start-all.sh

# 2) Run all Fabric CLI steps inside fabric-tools (configtxgen/peer available there)
echo "Running Fabric CLI steps in fabric-tools container..."
docker run --rm -t \
  --network test \
  -v "$ROOT_DIR":/work \
  -w /work/channel \
  hyperledger/fabric-tools:2.5 \
  bash -lc '
    set -e
    # Run your scripts that need configtxgen/peer
    ./1.create-genesis-block.sh
    ./2.create-channelTx.sh
    ./3.create-anchor-peer.sh
    ./5.create-app-channel.sh
    ./6.join-peers-to-channel.sh
    ./7.update-anchor-peers.sh
  '

# 3) Go modules + deploy chaincode also inside fabric-tools (peer exists there)
echo "Deploying chaincode in fabric-tools container..."
docker run --rm -t \
  --network test \
  -v "$ROOT_DIR":/work \
  -w /work \
  hyperledger/fabric-tools:2.5 \
  bash -lc '
    set -e
    cd /work/src/go
    if [ ! -f go.mod ]; then
      go mod init chaincode
    fi
    go mod tidy

    cd /work/deploy-chaincode
    ./deploy-chaincode.sh
  '

# 4) Start explorer (we'll fix port in next section)
echo "Starting Explorer..."
cd "$ROOT_DIR/channel/explorer"
docker compose up -d

echo "Explorer is up and running!"
echo "Setup successful!"
