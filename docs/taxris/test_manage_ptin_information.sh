#!/usr/bin/env bash
# Test: managePTinInformation

BASE="http://202.123.182.165:8080"
HASH_KEY="6eP4tCfELd5lGAU6713+ggL5xSqmbLFkotoQWPhGORE="

curl -s -X POST "$BASE/mediate/TaxRIS/managePTinInformation/ReqTinInfo" \
  -H "Content-Type: application/json" \
  -d "{
    \"ReqTinInfo\": {
      \"HASH_KEY\": \"$HASH_KEY\",
      \"SYS\": \"LMIS\",
      \"LABO_ID\": \"LABO-TEST-001\",
      \"TAXR_GV_NM\": \"Somchai\",
      \"TAXR_FAM_NM\": \"Vongkhamheng\",
      \"GND_TP\": \"M\",
      \"NAT_TP\": \"LA\",
      \"BDAY\": \"19900115\",
      \"TEL_NO\": \"\",
      \"HP_NO\": \"2055123456\",
      \"FAX_NO\": \"\",
      \"EMAIL\": \"\",
      \"IND_ID\": \"1234567890\",
      \"IND_ID_TP\": \"01\",
      \"FAMB_ISSU_PLC\": \"\",
      \"ADDR_SEQNO\": \"\",
      \"UNIT_NO\": \"\",
      \"ROAD_NM\": \"\",
      \"HOU_NO\": \"123\",
      \"PBOX_NO\": \"\",
      \"PUB_OFFI_YN\": \"N\",
      \"IND_BUSN_OPR_YN\": \"N\",
      \"PVT_CO_EMP_YN\": \"Y\",
      \"ETC_JOB_CONT\": \"\",
      \"DIVD_INC_YN\": \"N\",
      \"RENT_INC_YN\": \"N\",
      \"ETC_INC_CONT\": \"\",
      \"WORK_TIN\": \"\",
      \"WORK_ADDR_SEQNO\": \"\",
      \"WORK_UNIT_NO\": \"\",
      \"WORK_ROAD_NM\": \"\",
      \"WORK_HOU_NO\": \"\",
      \"BANK_ACC_NO\": \"\",
      \"SO_SE_NO\": \"\"
    }
  }" | python3 -m json.tool 2>/dev/null || echo "(raw response above)"
