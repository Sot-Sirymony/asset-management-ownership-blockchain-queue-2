#!/usr/bin/env bash
set -euo pipefail

CHANNEL_NAME="channel-org"
ORDERER_ADDR="orderer.ownify.com:7050"

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
CHANNEL_DIR="$ROOT_DIR/channel"

CHANNEL_TX="$CHANNEL_DIR/channel-artifacts/${CHANNEL_NAME}.tx"
CHANNEL_BLOCK="$CHANNEL_DIR/channel-artifacts/${CHANNEL_NAME}.block"

# quick guardrails
if [ ! -f "$CHANNEL_TX" ]; then
  echo "❌ Missing channel tx: $CHANNEL_TX"
  echo "   You currently have:"
  ls -la "$CHANNEL_DIR/channel-artifacts" || true
  exit 1
fi

echo "Creating application channel: ${CHANNEL_NAME}"

docker run --rm -t \
  --network test \
  -v "$ROOT_DIR":/work \
  -w /work/channel \
  hyperledger/fabric-tools:2.5 \
  bash -lc "
    set -e
    export FABRIC_CFG_PATH=\${PWD}/config
    export CORE_PEER_TLS_ENABLED=true
    export CORE_PEER_LOCALMSPID='Org1MSP'
    export CORE_PEER_MSPCONFIGPATH=\${PWD}/crypto-config/peerOrganizations/org1.ownify.com/users/Admin@org1.ownify.com/msp
    export CORE_PEER_ADDRESS=peer0.org1.ownify.com:7051
    export CORE_PEER_TLS_ROOTCERT_FILE=\${PWD}/crypto-config/peerOrganizations/org1.ownify.com/peers/peer0.org1.ownify.com/tls/ca.crt

    export ORDERER_CA=\${PWD}/crypto-config/ordererOrganizations/ownify.com/orderers/orderer.ownify.com/tls/ca.crt

    peer channel create \
      -o '${ORDERER_ADDR}' \
      -c '${CHANNEL_NAME}' \
      -f '${CHANNEL_TX}' \
      --outputBlock '${CHANNEL_BLOCK}' \
      --tls --cafile \"\$ORDERER_CA\"

    echo '✅ Channel block written to: ${CHANNEL_BLOCK}'
  "
