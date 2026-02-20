#!/usr/bin/env bash
set -u

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
NETWORK_DIR="$ROOT_DIR/ownership-network-master"
API_LOG="$ROOT_DIR/ownership-api-master/api.log"
UI_LOG="$ROOT_DIR/ownership-ui-master/ui.log"

is_port_listening() {
  local port="$1"

  if command -v lsof >/dev/null 2>&1; then
    lsof -Pi :"$port" -sTCP:LISTEN -t >/dev/null 2>&1
    return $?
  fi

  if command -v ss >/dev/null 2>&1; then
    ss -lnt "( sport = :$port )" | awk 'NR>1 {exit 0} END {exit 1}'
    return $?
  fi

  return 1
}

http_ok() {
  local url="$1"
  local code
  code="$(curl -s -o /dev/null -w '%{http_code}' "$url" 2>/dev/null || true)"
  [ "$code" = "200" ] || [ "$code" = "302" ]
}

echo "=== Overall Status ==="
echo

echo "[Docker]"
if docker info >/dev/null 2>&1; then
  echo "  Docker daemon: running"
else
  echo "  Docker daemon: NOT running or not accessible"
fi
echo

echo "[Blockchain Network]"
if [ -x "$NETWORK_DIR/net.sh" ]; then
  (cd "$NETWORK_DIR" && ./net.sh status) || echo "  Unable to read network status"
else
  echo "  net.sh not found at: $NETWORK_DIR/net.sh"
fi
if docker info >/dev/null 2>&1 && docker ps --format '{{.Names}}' | grep -q '^cli$'; then
  if docker exec cli peer channel getinfo -c channel-org >/dev/null 2>&1; then
    echo "  Channel check: channel-org available"
    if docker exec cli peer lifecycle chaincode querycommitted -C channel-org 2>/dev/null | grep -q "Name: basic,"; then
      echo "  Chaincode check: basic committed"
    else
      echo "  Chaincode check: basic not committed"
    fi
  else
    echo "  Channel check: channel-org missing/unavailable"
  fi
fi
echo

echo "[API - port 8081]"
if is_port_listening 8081; then
  echo "  Listener: running"
  if http_ok "http://localhost:8081/v3/api-docs"; then
    echo "  HTTP check: OK (api-docs reachable)"
  elif http_ok "http://localhost:8081/swagger-ui/index.html"; then
    echo "  HTTP check: OK (swagger reachable)"
  else
    echo "  HTTP check: listener up, docs endpoint not reachable yet"
  fi
else
  echo "  Listener: not running"
fi
if [ -f "$API_LOG" ]; then
  echo "  Log file: $API_LOG"
else
  echo "  Log file: not created yet"
fi
echo

echo "[PostgreSQL - port 55432/5432]"
if is_port_listening 55432; then
  echo "  Listener: running on 55432 (compose default)"
elif is_port_listening 5432; then
  echo "  Listener: running on 5432"
else
  echo "  Listener: not running"
fi
echo

echo "[UI - port 3000]"
if is_port_listening 3000; then
  echo "  Listener: running"
  if http_ok "http://localhost:3000"; then
    echo "  HTTP check: OK"
  else
    echo "  HTTP check: listener up, homepage not reachable yet"
  fi
else
  echo "  Listener: not running"
fi
if [ -f "$UI_LOG" ]; then
  echo "  Log file: $UI_LOG"
else
  echo "  Log file: not created yet"
fi
