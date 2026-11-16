#!/usr/bin/env bash
# Runs docs/taxris/test_issue_individual_tin3.sh ON the UAT server (SERVER_HOST in .uat.env,
# 172.16.0.247) over ssh, so TaxRIS is called from the server's network.
# Assumes this PC is already connected to the VPN (see vpn-connect.sh); it does not start it.
set -euo pipefail

DIR="$(cd "$(dirname "${BASH_SOURCE[0]:-$0}")" && pwd)"
ROOT="$DIR/.."

[ -f "$ROOT/.uat.env" ] || { echo "Missing $ROOT/.uat.env" >&2; exit 1; }

# Read values literally (not `source`): the password may contain $ and #, which bash would expand/strip.
get() { tr -d '\r' < "$ROOT/.uat.env" | grep -m1 "^$1=" | cut -d= -f2-; }
SERVER_HOST="$(get SERVER_HOST)"; SERVER_USER="$(get SERVER_USER)"; SERVER_PASSWORD="$(get SERVER_PASSWORD)"
[ -n "$SERVER_HOST" ] && [ -n "$SERVER_USER" ] && [ -n "$SERVER_PASSWORD" ] \
  || { echo "SERVER_HOST/SERVER_USER/SERVER_PASSWORD must be set in .uat.env" >&2; exit 1; }

# The script is streamed to the remote bash's stdin; nothing is copied to the server.
sshpass -p "$SERVER_PASSWORD" ssh -o StrictHostKeyChecking=accept-new -o ConnectTimeout=10 \
  "${SERVER_USER}@${SERVER_HOST}" 'bash -s' < "$ROOT/docs/taxris/test_issue_individual_tin3.sh"
