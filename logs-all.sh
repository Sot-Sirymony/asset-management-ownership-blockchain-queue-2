#!/usr/bin/env bash
set -u

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
NETWORK_DIR="$ROOT_DIR/ownership-network-master"
API_LOG="$ROOT_DIR/ownership-api-master/api.log"
UI_LOG="$ROOT_DIR/ownership-ui-master/ui.log"

usage() {
  echo "Usage: $0 [api|ui|network|network-ca|network-explorer|all]"
  echo
  echo "Examples:"
  echo "  $0 all"
  echo "  $0 api"
  echo "  $0 ui"
  echo "  $0 network"
  echo "  $0 network-ca"
  echo "  $0 network-explorer"
}

show_tail_or_note() {
  local file="$1"
  local label="$2"
  echo "=== $label ==="
  if [ -f "$file" ]; then
    tail -n 80 "$file"
  else
    echo "Log not found yet: $file"
  fi
  echo
}

target="${1:-all}"

case "$target" in
  api)
    if [ -f "$API_LOG" ]; then
      tail -f "$API_LOG"
    else
      echo "Log not found yet: $API_LOG"
      exit 1
    fi
    ;;
  ui)
    if [ -f "$UI_LOG" ]; then
      tail -f "$UI_LOG"
    else
      echo "Log not found yet: $UI_LOG"
      exit 1
    fi
    ;;
  network)
    if [ -x "$NETWORK_DIR/net.sh" ]; then
      (cd "$NETWORK_DIR" && ./net.sh logs)
    else
      echo "net.sh not found at: $NETWORK_DIR/net.sh"
      exit 1
    fi
    ;;
  network-ca)
    if [ -x "$NETWORK_DIR/net.sh" ]; then
      (cd "$NETWORK_DIR" && ./net.sh logs ca)
    else
      echo "net.sh not found at: $NETWORK_DIR/net.sh"
      exit 1
    fi
    ;;
  network-explorer)
    if [ -x "$NETWORK_DIR/net.sh" ]; then
      (cd "$NETWORK_DIR" && ./net.sh logs explorer)
    else
      echo "net.sh not found at: $NETWORK_DIR/net.sh"
      exit 1
    fi
    ;;
  all)
    show_tail_or_note "$API_LOG" "API (last 80 lines)"
    show_tail_or_note "$UI_LOG" "UI (last 80 lines)"
    echo "=== Network Status ==="
    if [ -x "$NETWORK_DIR/net.sh" ]; then
      (cd "$NETWORK_DIR" && ./net.sh status)
    else
      echo "net.sh not found at: $NETWORK_DIR/net.sh"
    fi
    echo
    echo "Use '$0 api' or '$0 ui' for live tail."
    echo "Use '$0 network' / 'network-ca' / 'network-explorer' for network logs."
    ;;
  *)
    usage
    exit 1
    ;;
esac
