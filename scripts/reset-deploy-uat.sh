#!/bin/bash
# Like deploy-uat.sh, but first WIPES the ptin schema on the UAT database
# (all users, profiles, applications), then redeploys; Flyway recreates it.
# Then seeds `superadmin` and `admin` (scripts/init-db.sql) with fresh random passwords, written to
# .uat-seed-credentials (mode 600, gitignored) and printed once.
#
# Usage: ./scripts/reset-deploy-uat.sh [--yes]
set -euo pipefail

if [ "${1:-}" != "--yes" ]; then
  echo "WARNING: this DROPS the 'ptin' schema (ALL DATA) on the UAT database, then redeploys."
  read -r -p "Type 'wipe' to continue: " answer
  [ "$answer" = "wipe" ] || { echo "Aborted."; exit 1; }
fi

# Random passwords, BCrypt-hashed locally; only the hashes leave this machine.
gen_pw() { python3 -c 'import secrets,string;print("".join(secrets.choice(string.ascii_letters+string.digits) for _ in range(16)))'; }
hash_pw() { python3 -c 'import bcrypt,sys;print(bcrypt.hashpw(sys.argv[1].encode(),bcrypt.gensalt(10)).decode())' "$1"; }
SUPERADMIN_PW="$(gen_pw)"; ADMIN_PW="$(gen_pw)"
export SEED_SUPERADMIN_HASH="$(hash_pw "$SUPERADMIN_PW")" SEED_ADMIN_HASH="$(hash_pw "$ADMIN_PW")"

CREDS="$(dirname "${BASH_SOURCE[0]}")/../.uat-seed-credentials"
( umask 077; printf 'superadmin / %s\nadmin / %s\n' "$SUPERADMIN_PW" "$ADMIN_PW" > "$CREDS" )
echo "UAT staff credentials (also saved to .uat-seed-credentials):"
cat "$CREDS"

CLEAN_DB=1 exec "$(dirname "${BASH_SOURCE[0]}")/deploy-uat.sh" --yes
