#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
NETWORK_DIR="$ROOT_DIR/ownership-network-master"
FULL_RESET="${1:-}"

echo "=== Reset All (safe clean) ==="
echo

if [ -x "$ROOT_DIR/stop-all-projects.sh" ]; then
  echo "[1/4] Stopping running services..."
  "$ROOT_DIR/stop-all-projects.sh" || true
else
  echo "[1/4] stop-all-projects.sh not found, skipping stop step."
fi

echo
echo "[2/4] Resetting blockchain network artifacts..."
if [ -x "$NETWORK_DIR/net.sh" ]; then
  (cd "$NETWORK_DIR" && ./net.sh reset)
else
  echo "net.sh not found at: $NETWORK_DIR/net.sh"
  exit 1
fi

echo
echo "[3/4] Cleaning application logs and build caches..."
rm -f "$ROOT_DIR/ownership-api-master/api.log"
rm -f "$ROOT_DIR/ownership-ui-master/ui.log"
rm -rf "$ROOT_DIR/ownership-api-master/target"
rm -rf "$ROOT_DIR/ownership-ui-master/.next"

echo
if [ "$FULL_RESET" = "--full" ]; then
  echo "[4/4] Full reset requested: removing UI node_modules..."
  rm -rf "$ROOT_DIR/ownership-ui-master/node_modules"
  echo "Full reset complete."
else
  echo "[4/4] Full reset not requested."
  echo "Tip: run '$0 --full' to also remove ownership-ui-master/node_modules."
fi

echo
echo "Reset completed."
echo "Next step: run ./start-all-projects.sh"
