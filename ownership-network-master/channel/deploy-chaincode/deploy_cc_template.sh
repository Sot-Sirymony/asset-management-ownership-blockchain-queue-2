#!/usr/bin/env bash
set -euo pipefail

CHANNEL_NAME="${CHANNEL_NAME:-channel-org}"
CC_NAME="${CC_NAME:-basic}"
CC_VERSION="${CC_VERSION:-1}"
CC_SEQUENCE="${CC_SEQUENCE:-1}"

DOCKER_NET="${DOCKER_NET:-test}"

# Host-relative chaincode path (from repo root)
CC_PATH_HOST_REL="${CC_PATH_HOST_REL:-src/go}"

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Detect crypto-config folder (host)
if [ -d "${ROOT_DIR}/channel/crypto-config" ]; then
  CRYPTO_HOST_REL="channel/crypto-config"
elif [ -d "${ROOT_DIR}/crypto-config" ]; then
  CRYPTO_HOST_REL="crypto-config"
else
  echo "ERROR: crypto-config not found under:"
  echo "  ${ROOT_DIR}/channel/crypto-config"
  echo "  ${ROOT_DIR}/crypto-config"
  echo
  echo "Run: find . -maxdepth 4 -type d -name crypto-config"
  exit 1
fi

# Paths inside container (because we mount repo root to /work)
CRYPTO_BASE="/work/${CRYPTO_HOST_REL}"
PEER_TLS_CA="${CRYPTO_BASE}/peerOrganizations/org1.ownify.com/peers/peer0.org1.ownify.com/tls/ca.crt"
ADMIN_MSP="${CRYPTO_BASE}/peerOrganizations/org1.ownify.com/users/Admin@org1.ownify.com/msp"
ORDERER_TLS_CA="${CRYPTO_BASE}/ordererOrganizations/ownify.com/orderers/orderer.ownify.com/msp/tlscacerts/tlsca.ownify.com-cert.pem"

ORG_MSP="${ORG_MSP:-Org1MSP}"
PEER_ADDR="${PEER_ADDR:-peer0.org1.ownify.com:7051}"
ORDERER_ADDR="${ORDERER_ADDR:-orderer.ownify.com:7050}"

echo "ROOT_DIR: ${ROOT_DIR}"
echo "Using crypto-config: ${CRYPTO_HOST_REL}"
echo "Chaincode path: ${CC_PATH_HOST_REL}"
echo

docker run --rm -it \
  --network "${DOCKER_NET}" \
  -v "${ROOT_DIR}":/work \
  -w /work \
  hyperledger/fabric-tools:2.5 \
  bash -lc "
set -euo pipefail

echo '=== Check chaincode folder ==='
ls -la \"/work/${CC_PATH_HOST_REL}\" | head -50

echo '=== Check crypto files ==='
ls -la \"${PEER_TLS_CA}\"
ls -la \"${ADMIN_MSP}\"
ls -la \"${ORDERER_TLS_CA}\"

echo '=== Set peer env ==='
export CORE_PEER_TLS_ENABLED=true
export CORE_PEER_LOCALMSPID='${ORG_MSP}'
export CORE_PEER_ADDRESS='${PEER_ADDR}'
export CORE_PEER_TLS_ROOTCERT_FILE='${PEER_TLS_CA}'
export CORE_PEER_MSPCONFIGPATH='${ADMIN_MSP}'

echo '=== Package ==='
rm -f ${CC_NAME}.tar.gz log.txt || true
peer lifecycle chaincode package ${CC_NAME}.tar.gz \
  --path \"/work/${CC_PATH_HOST_REL}\" \
  --lang golang \
  --label ${CC_NAME}_${CC_VERSION}

echo '=== Install ==='
peer lifecycle chaincode install ${CC_NAME}.tar.gz

echo '=== Query installed ==='
peer lifecycle chaincode queryinstalled > log.txt
cat log.txt

PACKAGE_ID=\$(sed -n \"/${CC_NAME}_${CC_VERSION}/{s/^Package ID: //; s/, Label:.*$//; p;}\" log.txt)
if [ -z \"\${PACKAGE_ID}\" ]; then
  echo 'ERROR: PACKAGE_ID not found'
  exit 1
fi
echo \"PACKAGE_ID=\${PACKAGE_ID}\"

echo '=== Approve ==='
peer lifecycle chaincode approveformyorg \
  -o '${ORDERER_ADDR}' \
  --tls --cafile '${ORDERER_TLS_CA}' \
  --channelID '${CHANNEL_NAME}' \
  --name '${CC_NAME}' \
  --version '${CC_VERSION}' \
  --sequence '${CC_SEQUENCE}' \
  --package-id \"\${PACKAGE_ID}\" \
  --init-required

echo '=== Commit ==='
peer lifecycle chaincode commit \
  -o '${ORDERER_ADDR}' \
  --tls --cafile '${ORDERER_TLS_CA}' \
  --channelID '${CHANNEL_NAME}' \
  --name '${CC_NAME}' \
  --peerAddresses '${PEER_ADDR}' \
  --tlsRootCertFiles '${PEER_TLS_CA}' \
  --version '${CC_VERSION}' \
  --sequence '${CC_SEQUENCE}' \
  --init-required

echo '=== Query committed (must show ${CC_NAME}) ==='
peer lifecycle chaincode querycommitted -C '${CHANNEL_NAME}'

echo '=== Init (safe to run; may fail if already initialized) ==='
peer chaincode invoke \
  -o '${ORDERER_ADDR}' \
  --tls --cafile '${ORDERER_TLS_CA}' \
  -C '${CHANNEL_NAME}' \
  -n '${CC_NAME}' \
  --peerAddresses '${PEER_ADDR}' \
  --tlsRootCertFiles '${PEER_TLS_CA}' \
  --isInit \
  -c '{\"Args\":[]}' || true

echo 'DONE.'
"
