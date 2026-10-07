#!/usr/bin/env bash
# Test: callAddress — list districts under a province (ACT_CD=D)
# Change ADDR_CD to the province code you want to query.

BASE="http://202.123.182.165:8080"
HASH_KEY="6eP4tCfELd5lGAU6713+ggL5xSqmbLFkotoQWPhGORE="
PROVINCE_CODE="${1:-01}"

curl -s -X POST "$BASE/mediate/TaxRIS/callAddress/ReqADD" \
  -H "Content-Type: application/json" \
  -d "{
    \"ReqADD\": {
      \"HASH_KEY\": \"$HASH_KEY\",
      \"ACT_CD\": \"D\",
      \"ADDR_CD\": \"$PROVINCE_CODE\"
    }
  }" | python3 -m json.tool 2>/dev/null || echo "(raw response above)"
