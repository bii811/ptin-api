#!/bin/bash
# Like deploy-uat.sh, but first WIPES the ptin schema on the UAT database
# (all users, profiles, applications), then redeploys; Flyway recreates it.
# Afterwards re-seed the admin: scripts/init-db.sql.
#
# Usage: ./scripts/reset-deploy-uat.sh [--yes]
set -euo pipefail

if [ "${1:-}" != "--yes" ]; then
  echo "WARNING: this DROPS the 'ptin' schema (ALL DATA) on the UAT database, then redeploys."
  read -r -p "Type 'wipe' to continue: " answer
  [ "$answer" = "wipe" ] || { echo "Aborted."; exit 1; }
fi

CLEAN_DB=1 exec "$(dirname "${BASH_SOURCE[0]}")/deploy-uat.sh" --yes
