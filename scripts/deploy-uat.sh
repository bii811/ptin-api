#!/bin/bash
# Build the current version as a UAT image and deploy it to the UAT app
# server (172.16.0.247, see .uat.env), replacing any running "ptin-api"
# container. Tails the new container's logs until Ctrl+C, at which point the
# VPN tunnel used to reach the server is disconnected.
#
# Set CLEAN_DB=1 to also drop+recreate the ptin schema before starting the new
# container (Flyway rebuilds it on boot) — see scripts/reset-deploy-uat.sh.
#
# Usage: ./scripts/deploy-uat.sh [--yes]
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

# --- VPN (same FortiGate SSL-VPN as scripts/vpn-connect.sh) ---
VPN_HOST="115.84.65.34:10443"
VPN_USER="poulim"
VPN_PASS="pl@123456"     # better: read from env var or a secrets file
VPN_PID_FILE="/tmp/vpn.pid"

# --- UAT deploy target + app env vars (.uat.env, untracked/never committed) ---
set -a
source "${REPO_ROOT}/.uat.env"
set +a
# Provides: SERVER_HOST, SERVER_USER, SERVER_PASSWORD, PTIN_DB_URL,
# PTIN_DB_USERNAME, PTIN_DB_PASSWORD

VERSION="$(grep -m1 '^version' "${REPO_ROOT}/build.gradle.kts" | sed -E 's/.*"(.*)".*/\1/')"
TAG="${VERSION}-uat"
IMAGE="ptin:${TAG}"
CONTAINER="ptin-api"
HOST_PORT=8087

LOCAL_TAR="/tmp/ptin-${TAG}.tar"
REMOTE_TAR="/tmp/ptin-${TAG}.tar"
REMOTE_ENV="/tmp/ptin-api.env"
# App-only env for the running container — drop the SERVER_* deploy
# credentials from .uat.env, they're for reaching the box, not for the app.
LOCAL_ENV="/tmp/ptin-api.env"

if [ "${1:-}" != "--yes" ]; then
  echo "This will replace the running '${CONTAINER}' container on ${SERVER_HOST} with ${IMAGE}."
  read -r -p "Continue? [y/N] " answer
  case "$answer" in
    [yY]|[yY][eE][sS]) ;;
    *) echo "Aborted."; exit 1 ;;
  esac
fi

cleanup() {
  echo
  echo "Disconnecting VPN..."
  if [ -f "$VPN_PID_FILE" ]; then
    VPN_PID="$(cat "$VPN_PID_FILE")"
    if sudo kill "$VPN_PID" 2>/dev/null; then
      echo "Killed VPN process (pid $VPN_PID)."
    else
      echo "No process running for pid $VPN_PID (already down?)."
    fi
    rm -f "$VPN_PID_FILE"
  else
    if VPN_PID="$(pgrep -x openconnect)"; then
      sudo kill $VPN_PID
      echo "Killed openconnect process(es): $VPN_PID"
    else
      echo "No openconnect process found."
    fi
  fi
}
trap cleanup EXIT INT TERM

# 1. Build the image — the Dockerfile's PROFILE build-arg bakes
# SPRING_PROFILES_ACTIVE=uat as an image ENV.
echo "Building ${IMAGE}..."
docker build --build-arg PROFILE=uat -t "$IMAGE" -f "${REPO_ROOT}/Dockerfile" "${REPO_ROOT}"

# 2. Connect VPN if not already up.
if pgrep -x openconnect >/dev/null 2>&1; then
  echo "VPN already connected."
else
  echo "Connecting to VPN..."
  echo "$VPN_PASS" | sudo openconnect --background --pid-file="$VPN_PID_FILE" \
    --protocol=fortinet \
    --servercert pin-sha256:1V7B8PMKMj+0TM7gJlRciLwI0Qt2M1x9iw4oBU4d4wA= \
    --user="$VPN_USER" --passwd-on-stdin "$VPN_HOST"
  sleep 5
fi

# 3. Package image + runtime env, copy to server.
grep -v '^SERVER_' "${REPO_ROOT}/.uat.env" > "$LOCAL_ENV"

echo "Saving image to ${LOCAL_TAR}..."
docker save -o "$LOCAL_TAR" "$IMAGE"

echo "Copying image + env to ${SERVER_HOST}..."
sshpass -p "$SERVER_PASSWORD" scp -o StrictHostKeyChecking=accept-new \
  "$LOCAL_TAR" "${SERVER_USER}@${SERVER_HOST}:${REMOTE_TAR}"
sshpass -p "$SERVER_PASSWORD" scp -o StrictHostKeyChecking=accept-new \
  "$LOCAL_ENV" "${SERVER_USER}@${SERVER_HOST}:${REMOTE_ENV}"
rm -f "$LOCAL_TAR" "$LOCAL_ENV"

# 4-7. Stop old container, drop old image tag, load new image, run it.
echo "Deploying on ${SERVER_HOST}..."
sshpass -p "$SERVER_PASSWORD" ssh -o StrictHostKeyChecking=accept-new "${SERVER_USER}@${SERVER_HOST}" \
  "CLEAN_DB='${CLEAN_DB:-}' IMAGE='$IMAGE' CONTAINER='$CONTAINER' TAR='$REMOTE_TAR' ENVFILE='$REMOTE_ENV' PORT='$HOST_PORT' bash -s" <<'EOF'
set -euo pipefail
echo "Stopping existing container (if any)..."
docker rm -f "$CONTAINER" 2>/dev/null || true

if [ -n "$CLEAN_DB" ]; then
  echo "Dropping and recreating schema ptin..."
  set -a; . "$ENVFILE"; set +a
  # jdbc:postgresql://host:port/db?currentSchema=ptin
  hostport="${PTIN_DB_URL#jdbc:postgresql://}"; hostport="${hostport%%/*}"
  db="${PTIN_DB_URL#jdbc:postgresql://*/}"; db="${db%%\?*}"
  docker run --rm --network host -e PGPASSWORD="$PTIN_DB_PASSWORD" postgres:17-alpine \
    psql -v ON_ERROR_STOP=1 -h "${hostport%:*}" -p "${hostport#*:}" -U "$PTIN_DB_USERNAME" -d "$db" \
    -c 'DROP SCHEMA IF EXISTS ptin CASCADE' -c 'CREATE SCHEMA ptin'
fi

echo "Removing existing image tag (if any)..."
docker rmi "$IMAGE" 2>/dev/null || true

echo "Loading new image..."
docker load -i "$TAR"
rm -f "$TAR"

echo "Starting container..."
docker run -d -p "${PORT}:8080" --name "$CONTAINER" --restart=always --env-file "$ENVFILE" "$IMAGE"
EOF

# 8. Tail logs until Ctrl+C (then cleanup() disconnects the VPN, step 9).
echo "Deployed. Tailing logs (Ctrl+C to stop and disconnect VPN)..."
sshpass -p "$SERVER_PASSWORD" ssh -o StrictHostKeyChecking=accept-new "${SERVER_USER}@${SERVER_HOST}" \
  "docker logs -f $CONTAINER"
