#!/usr/bin/env bash
# Runs docs/taxris/test_issue_individual_tin3.sh ON the UAT server (SERVER_HOST in .uat.env,
# 172.16.0.247) over ssh, so TaxRIS is called from the server's network.
# Assumes this PC is already connected to the VPN (see vpn-connect.sh); it does not start it.
set -euo pipefail

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$DIR/.."

# Provides SERVER_HOST, SERVER_USER, SERVER_PASSWORD (tr: tolerate CRLF)
set -a
source <(tr -d '\r' < "$ROOT/.uat.env")
set +a

# The script is streamed to the remote bash's stdin; nothing is copied to the server.
sshpass -p "$SERVER_PASSWORD" ssh -o StrictHostKeyChecking=accept-new -o ConnectTimeout=10 \
  "${SERVER_USER}@${SERVER_HOST}" 'bash -s' < "$ROOT/docs/taxris/test_issue_individual_tin3.sh"
