#!/usr/bin/env bash
# Test: callAddress — list provinces (ACT_CD=P)

BASE="http://202.123.182.165:8080"
HASH_KEY="6eP4tCfELd5lGAU6713+ggL5xSqmbLFkotoQWPhGORE="

curl -s -X POST "$BASE/mediate/TaxRIS/callAddress/ReqADD" \
  -H "Content-Type: application/json" \
  -d "{
    \"ReqADD\": {
      \"HASH_KEY\": \"$HASH_KEY\",
      \"ACT_CD\": \"P\",
      \"ADDR_CD\": \"\"
    }
  }" | python3 -m json.tool 2>/dev/null || echo "(raw response above)"
