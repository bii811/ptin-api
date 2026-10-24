#!/usr/bin/env bash
# Connects the VPN (FortiGate SSL-VPN, same as vpn-connect.sh), runs
# docs/taxris/test_issue_individual_tin3.sh against TaxRIS, then always
# disconnects the VPN on exit (success, failure or Ctrl+C).
set -euo pipefail

DIR="$(cd "$(dirname "$0")" && pwd)"
VPN_HOST="115.84.65.34:10443"
VPN_USER="poulim"
VPN_PASS="pl@123456"     # better: read from env var or a secrets file
PID_FILE="/tmp/vpn.pid"

cleanup() {
  echo
  echo "Disconnecting VPN..."
  if [ -f "$PID_FILE" ]; then
    VPN_PID="$(cat "$PID_FILE")"
    if sudo kill "$VPN_PID" 2>/dev/null; then
      echo "Killed VPN process (pid $VPN_PID)."
    else
      echo "No process running for pid $VPN_PID (already down?)."
    fi
    rm -f "$PID_FILE"
  else
    echo "No PID file found; searching for a running openconnect process..."
    if VPN_PID="$(pgrep -x openconnect)"; then
      sudo kill $VPN_PID
      echo "Killed openconnect process(es): $VPN_PID"
    else
      echo "No openconnect process found."
    fi
  fi
}
trap cleanup EXIT INT TERM

echo "Connecting to VPN..."
echo "$VPN_PASS" | sudo openconnect --background --pid-file="$PID_FILE" \
  --protocol=fortinet \
  --servercert pin-sha256:1V7B8PMKMj+0TM7gJlRciLwI0Qt2M1x9iw4oBU4d4wA= \
  --user="$VPN_USER" --passwd-on-stdin "$VPN_HOST"

sleep 5   # give the tunnel a moment to come up
echo "VPN connected. Running test..."

bash "$DIR/../docs/taxris/test_issue_individual_tin3.sh"
