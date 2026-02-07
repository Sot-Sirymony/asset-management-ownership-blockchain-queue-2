#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
NET_NAME="test"

ensure_network() {
  docker network ls | grep -q " ${NET_NAME}$" || docker network create "${NET_NAME}"
}

status() {
  docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
}

up_ca() {
  ensure_network
  docker compose -f "$ROOT_DIR/create-certificate-with-ca/docker-compose.yaml" up -d ca_orderer ca_org1
}

gen_crypto() {
  ensure_network

  docker run --rm -t --network "$NET_NAME" \
    -v "$ROOT_DIR":/work -w /work/create-certificate-with-ca/orderer \
    hyperledger/fabric-ca:1.5 \
    bash -lc "chmod +x ./start-orderers-v3.sh && ./start-orderers-v3.sh"

  docker run --rm -t --network "$NET_NAME" \
    -v "$ROOT_DIR":/work -w /work/create-certificate-with-ca/org1 \
    hyperledger/fabric-ca:1.5 \
    bash -lc "chmod +x ./start-orgs.sh && ./start-orgs.sh"
}

up_fabric() {
  ensure_network
  (cd "$ROOT_DIR/channel" && ./start-all.sh)
}

channel_and_join() {
  ensure_network
  docker run --rm -t --network "$NET_NAME" \
    -v "$ROOT_DIR":/work -w /work/channel \
    hyperledger/fabric-tools:2.5 \
    bash -lc '
      set -e
      chmod +x *.sh
      ./1.create-genesis-block.sh
      ./2.create-channelTx.sh
      ./3.create-anchor-peer.sh
      ./5.create-app-channel.sh
      ./6.join-peers-to-channel.sh
      ./7.update-anchor-peers.sh
    '
}

deploy_cc() {
  ensure_network
  docker run --rm -t --network "$NET_NAME" \
    -v "$ROOT_DIR":/work -w /work \
    hyperledger/fabric-tools:2.5 \
    bash -lc '
      set -e
      cd /work/src/go
      [ -f go.mod ] || go mod init chaincode
      go mod tidy
      cd /work/deploy-chaincode
      chmod +x *.sh
      ./deploy-chaincode.sh
    '
}

up_explorer() {
  ensure_network
  (cd "$ROOT_DIR/channel/explorer" && docker compose up -d)
  echo "Explorer: http://localhost:8080  (exploreradmin / exploreradminpw / first-network)"
}

down() {
  (cd "$ROOT_DIR/channel/explorer" && docker compose down || true)
  (cd "$ROOT_DIR/channel" && docker compose down || true)
  docker compose -f "$ROOT_DIR/create-certificate-with-ca/docker-compose.yaml" down || true
}

reset() {
  down
  (cd "$ROOT_DIR/channel/explorer" && docker compose down -v || true)
  rm -rf "$ROOT_DIR/channel/crypto-config"
  echo "✅ Reset done."
}

logs() {
  local what="${1:-}"
  case "$what" in
    explorer) (cd "$ROOT_DIR/channel/explorer" && docker compose logs -f explorer.mynetwork.com) ;;
    explorerdb) (cd "$ROOT_DIR/channel/explorer" && docker compose logs -f explorerdb.mynetwork.com) ;;
    ca) docker compose -f "$ROOT_DIR/create-certificate-with-ca/docker-compose.yaml" logs -f ;;
    *) status ;;
  esac
}

cmd="${1:-}"
case "$cmd" in
  up) up_ca; gen_crypto; up_fabric; channel_and_join; deploy_cc; up_explorer ;;
  up-ca) up_ca ;;
  gen-crypto) gen_crypto ;;
  up-fabric) up_fabric ;;
  channel) channel_and_join ;;
  deploy-cc) deploy_cc ;;
  up-explorer) up_explorer ;;
  down) down ;;
  reset) reset ;;
  status) status ;;
  logs) shift; logs "${1:-}" ;;
  *)
    echo "Usage: $0 {up|up-ca|gen-crypto|up-fabric|channel|deploy-cc|up-explorer|down|reset|status|logs explorer|logs ca}"
    exit 1
    ;;
esac
