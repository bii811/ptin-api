#!/bin/bash
# Connects the VPN (same FortiGate SSL-VPN used by e-filing-api's
# vpn-init-db-dfiling-test.sh) and hangs in the foreground, keeping the
# tunnel up until you press Ctrl+C or otherwise kill this process — at which
# point the VPN is disconnected automatically.
set -euo pipefail

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
# --protocol=fortinet: gateway is a FortiGate SSL-VPN (port 10443 is its default).
# No --certificate flag: auth is username/password only, no client certificate.
echo "$VPN_PASS" | sudo openconnect --background --pid-file="$PID_FILE" \
  --protocol=fortinet \
  --servercert pin-sha256:1V7B8PMKMj+0TM7gJlRciLwI0Qt2M1x9iw4oBU4d4wA= \
  --user="$VPN_USER" --passwd-on-stdin "$VPN_HOST"

sleep 5   # give the tunnel a moment to come up

echo "VPN connected. Press Ctrl+C to disconnect and exit."
# Hang until interrupted; cleanup() runs via the trap above.
while true; do
  sleep 3600
done
