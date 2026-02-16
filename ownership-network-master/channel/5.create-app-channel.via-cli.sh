#!/usr/bin/env bash
set -euo pipefail

CHANNEL_NAME="${CHANNEL_NAME:-channel-org}"
ORDERER_ADDR="${ORDERER_ADDR:-orderer.ownify.com:7050}"

docker exec -it cli bash -lc "
set -euo pipefail

export CHANNEL_NAME='${CHANNEL_NAME}'
export ORDERER_ADDR='${ORDERER_ADDR}'

export FABRIC_CFG_PATH=/etc/hyperledger/fabric/config
export CORE_PEER_TLS_ENABLED=true
export CORE_PEER_LOCALMSPID='Org1MSP'
export CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.ownify.com/users/Admin@org1.ownify.com/msp
export CORE_PEER_ADDRESS=peer0.org1.ownify.com:7051
export CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.ownify.com/peers/peer0.org1.ownify.com/tls/ca.crt

# Use TLS CA (tlsca) as cafile (more stable)
export ORDERER_CA=/etc/hyperledger/fabric/crypto-config/ordererOrganizations/ownify.com/orderers/orderer.ownify.com/tls/tlscacerts/tls-ca_orderer-9054-ca-orderer.pem

export CHANNEL_TX=/etc/hyperledger/fabric/channel-artifacts/\${CHANNEL_NAME}.tx
export CHANNEL_BLOCK=/etc/hyperledger/fabric/channel-artifacts/\${CHANNEL_NAME}.block

echo 'Creating application channel:' \${CHANNEL_NAME}
peer channel create \
  -o \${ORDERER_ADDR} \
  -c \${CHANNEL_NAME} \
  -f \${CHANNEL_TX} \
  --outputBlock \${CHANNEL_BLOCK} \
  --tls --cafile \${ORDERER_CA}

echo '✅ Channel block written to:' \${CHANNEL_BLOCK}
"
