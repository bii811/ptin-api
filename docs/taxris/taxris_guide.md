# TaxRIS Integration Guide

Source: `TaxRIS-EI-DE-05(Interface_Specification)_PTIN_V3.xlsx`

TaxRIS exposes a mediation layer over SOAP and RESTful. PTIN acts as the **caller** for TIN
issuance; TaxRIS is always the **callee**. All endpoints share the same host/port placeholder
`http://[IP address of server]:[port]`.

---

## UAT Credentials

| Property       | Value |
|----------------|-------|
| Base URL       | `http://202.123.182.165:8080` |
| Hash Key       | `6eP4tCfELd5lGAU6713+ggL5xSqmbLFkotoQWPhGORE=` |
| Hash Key (backup) | `4Z79Dkk13fGwfrxNhc2GgPHec6giw96VLrY9evrNHfo=` |

---

## Authentication

Every request must include `HASH_KEY` — a fixed authorization token assigned per agency to
distinguish callers. It goes in the request depot (`ReqTinInfo` / `ReqADD`) as a top-level field.

---

## Service 1 — `issueIndividualTin`

Issues a new personal TIN (PTIN → TaxRIS).

| Property             | Value |
|----------------------|-------|
| Caller               | PTIN  |
| Callee               | TaxRIS |
| Processing Type      | Online |
| Frequency            | Serially called after 6:00 PM |
| Counter-part         | IFID  |
| SOAP URI             | `.../mediate/TaxRIS/issueIndividualTin` |
| RESTful URI          | `.../mediate/TaxRIS/issueIndividualTin/ReqTinInfo` |
| WSDL                 | `.../mediate/TaxRIS/issueIndividualTin?wsdl` |
| WADL                 | `.../mediate/TaxRIS/issueIndividualTin?wadl` |

### Request — `ReqTinInfo`

| Key Name        | Attribute Name                                         | Type          | Mult | Notes |
|-----------------|--------------------------------------------------------|---------------|------|-------|
| `HASH_KEY`      | Hash Key                                               | STRING(80)    | 1    | Fixed auth token per agency |
| `TAXR_GV_NM`   | Given Name                                             | STRING(200)   | 1    | |
| `TAXR_FAM_NM`  | Family Name                                            | STRING(200)   | 1    | |
| `GND_TP`        | Gender                                                 | STRING(1)     | 1    | `M` = Male, `F` = Female |
| `NAT_TP`        | Nationality                                            | STRING(2)     | 1    | ISO country code |
| `BDAY`          | Birthday                                               | STRING(8)     | 1    | YYYYMMDD |
| `TEL_NO`        | Telephone Number                                       | STRING(20)    | 1    | |
| `HP_NO`         | Mobile Phone Number                                    | STRING(20)    | 1    | |
| `FAX_NO`        | Fax Number                                             | STRING(20)    | 1    | |
| `EMAIL`         | Email                                                  | STRING(200)   | 1    | |
| `IND_ID`        | Individual Identification Number (External Agency)     | STRING(20)    | 1    | e.g. national ID number |
| `IND_ID_TP`     | Individual Identification Type                         | STRING(2)     | 1    | |
| `FAMB_ISSU_PLC` | Laos Family Book Issuance Place                       | STRING(300)   | 1    | |
| `ADDR_SEQNO`    | Address Sequence Number                                | STRING(8)     | 1    | |
| `UNIT_NO`       | Unit Number (below base address)                       | STRING(3)     | 1    | |
| `ROAD_NM`       | Road Name                                              | STRING(200)   | 1    | |
| `HOU_NO`        | House Number                                           | STRING(200)   | 1    | |
| `PBOX_NO`       | P.O. Box                                               | STRING(10)    | 1    | |
| `PUB_OFFI_YN`  | Civil Servant?                                         | STRING(1)     | 1    | `Y` / `N` |
| `IND_BUSN_OPR_YN` | Independent/Freelance?                             | STRING(1)     | 1    | `Y` / `N` |
| `PVT_CO_EMP_YN` | Private Employee?                                     | STRING(1)     | 1    | `Y` / `N` |
| `ETC_JOB_CONT`  | Other Jobs (description)                               | STRING(2000)  | 1    | |
| `DIVD_INC_YN`  | Dividend Income?                                       | STRING(1)     | 1    | `Y` / `N` |
| `RENT_INC_YN`  | Lease/Rental Income?                                   | STRING(1)     | 1    | `Y` / `N` |
| `ETC_INC_CONT`  | Other Income (description)                             | STRING(2000)  | 1    | |
| `WORK_TIN`      | Working Company's TIN                                  | STRING(12)    | 1    | |
| `WORK_ADDR_SEQNO` | Working Place Address Sequence Number              | STRING(8)     | 1    | |
| `WORK_UNIT_NO`  | Working Place Unit Number                              | STRING(3)     | 1    | |
| `WORK_ROAD_NM`  | Working Place Road Name                                | STRING(200)   | 1    | |
| `WORK_HOU_NO`   | Working Place House Number                             | STRING(200)   | 1    | |
| `BANK_ACC_NO`   | Bank Account Number                                    | STRING(28)    | 1    | |
| `SO_SE_NO`      | SOSE Card Number                                       | STRING(20)    | 1    | |

### Response — `ResTaxRIS`

| Key Name          | Sub-key      | Attribute Name                | Type        | Mult | Notes |
|-------------------|--------------|-------------------------------|-------------|------|-------|
| `Result`          | `CD`         | Result Code                   | STRING(3)   | 1    | See return codes |
|                   | `MSG`        | Result Message                | STRING(200) | 1    | Detail message |
|                   | `CNT`        | Result Count                  | NUMBER(5)   | 1    | Fixed to `1` for this service |
| `TinInfo`         | `TIN`        | Taxpayer Identification Number| STRING(12)  | 1    | Issued TIN |
|                   | `TAXR_GV_NM` | Given Name                    | STRING(200) | 1    | |
|                   | `TAXR_FAM_NM`| Family Name                   | STRING(200) | 1    | |

---

## Service 2 — `managePTinInformation`

Manages/updates PTIN information, including cases where PTIN acts on behalf of LMIS
(Labor Management Information System). Shares the same field set as Service 1 with two
additional fields: `SYS` and `LABO_ID`.

| Property             | Value |
|----------------------|-------|
| Caller               | PTIN  |
| Callee               | TaxRIS |
| Processing Type      | Online |
| Frequency            | Serially called after 6:00 PM |
| Counter-part         | IFID  |
| SOAP URI             | `.../mediate/TaxRIS/managePTinInformation` |
| RESTful URI          | `.../mediate/TaxRIS/managePTinInformation/ReqTinInfo` |
| WSDL                 | `.../mediate/TaxRIS/managePTinInformation?wsdl` |
| WADL                 | `.../mediate/TaxRIS/managePTinInformation?wadl` |

### Request — `ReqTinInfo` (additional fields vs Service 1)

| Key Name   | Attribute Name | Type       | Mult | Notes |
|------------|----------------|------------|------|-------|
| `HASH_KEY` | Hash Key       | STRING(80) | 1    | Fixed auth token |
| `SYS`      | System Name    | STRING(10) | 1    | Fixed value: `LMIS` |
| `LABO_ID`  | Labor ID       | STRING(20) | 1    | LMIS internal identifier |

All remaining fields are identical to Service 1 (`TAXR_GV_NM` through `SO_SE_NO`).

### Response — `ResTaxRIS`

Identical to Service 1.

---

## Service 3 — `callAddress`

Returns address lookup data (province/district hierarchy) from TaxRIS.

| Property             | Value |
|----------------------|-------|
| Caller               | LMIS  |
| Callee               | TaxRIS |
| Processing Type      | Online |
| Frequency            | Random |
| Counter-part         | FICT  |
| SOAP URI             | `.../mediate/TaxRIS/callAddress` |
| RESTful URI          | `.../mediate/TaxRIS/callAddress/ReqADD` |
| WSDL                 | `.../mediate/TaxRIS/callAddress?wsdl` |
| WADL                 | `.../mediate/callAddress?wadl` |

### Request — `ReqADD`

| Key Name    | Attribute Name | Type       | Mult | Notes |
|-------------|----------------|------------|------|-------|
| `HASH_KEY`  | Hash Key       | STRING(80) | 1    | Fixed auth token |
| `ACT_CD`    | Action Code    | STRING(1)  | 1    | Address level: `P` = Province, `D` = District |
| `ADDR_CD`   | Address Code   | STRING(10) | 1    | Parent address code to filter by |

### Response — `ResTaxRIS`

| Key Name    | Sub-key        | Attribute Name   | Type         | Mult | Notes |
|-------------|----------------|------------------|--------------|------|-------|
| `Result`    | `CD`           | Result Code      | STRING(3)    | 1    | See return codes |
|             | `MSG`          | Result Message   | STRING(200)  | 1    | Detail message |
|             | `CNT`          | Result Count     | NUMBER(5)    | 1    | Total matched count |
| `Address`   | `ADDR_LVL_TP`  | Address Level    | STRING(1)    | 1..n | Repeating list |
|             | `ADDR_CD`      | Address Code     | STRING(10)   | 1..n | |
|             | `ADDR_CD_NM`   | Address Name     | STRING(1000) | 1..n | |

---

## REST API — How to Call

All three services follow the same pattern: `POST` with `Content-Type: application/json`.
The depot name (e.g. `ReqTinInfo`, `ReqADD`) is the **top-level JSON key** wrapping all fields.

### Headers

```http
POST <endpoint>
Content-Type: application/json
```

> No `Authorization` header — authentication is entirely via the `HASH_KEY` field inside the body.

---

### POST `/mediate/TaxRIS/issueIndividualTin/ReqTinInfo`

**UAT:** `http://202.123.182.165:8080/mediate/TaxRIS/issueIndividualTin/ReqTinInfo`

**Request body:**

```json
{
  "ReqTinInfo": {
    "HASH_KEY": "6eP4tCfELd5lGAU6713+ggL5xSqmbLFkotoQWPhGORE=",
    "TAXR_GV_NM": "Somchai",
    "TAXR_FAM_NM": "Vongkhamheng",
    "GND_TP": "M",
    "NAT_TP": "LA",
    "BDAY": "19900115",
    "TEL_NO": "",
    "HP_NO": "2055123456",
    "FAX_NO": "",
    "EMAIL": "",
    "IND_ID": "1234567890",
    "IND_ID_TP": "01",
    "FAMB_ISSU_PLC": "",
    "ADDR_SEQNO": "",
    "UNIT_NO": "",
    "ROAD_NM": "",
    "HOU_NO": "123",
    "PBOX_NO": "",
    "PUB_OFFI_YN": "N",
    "IND_BUSN_OPR_YN": "N",
    "PVT_CO_EMP_YN": "Y",
    "ETC_JOB_CONT": "",
    "DIVD_INC_YN": "N",
    "RENT_INC_YN": "N",
    "ETC_INC_CONT": "",
    "WORK_TIN": "123456789012",
    "WORK_ADDR_SEQNO": "",
    "WORK_UNIT_NO": "",
    "WORK_ROAD_NM": "",
    "WORK_HOU_NO": "",
    "BANK_ACC_NO": "",
    "SO_SE_NO": ""
  }
}
```

**Success response:**

```json
{
  "ResTaxRIS": {
    "Result": {
      "CD": "000",
      "MSG": "Success",
      "CNT": "1"
    },
    "TinInfo": {
      "TIN": "100000000001",
      "TAXR_GV_NM": "Somchai",
      "TAXR_FAM_NM": "Vongkhamheng"
    }
  }
}
```

---

### POST `/mediate/TaxRIS/managePTinInformation/ReqTinInfo`

**UAT:** `http://202.123.182.165:8080/mediate/TaxRIS/managePTinInformation/ReqTinInfo`

Same body as `issueIndividualTin` with two extra fields at the top:

```json
{
  "ReqTinInfo": {
    "HASH_KEY": "6eP4tCfELd5lGAU6713+ggL5xSqmbLFkotoQWPhGORE=",
    "SYS": "LMIS",
    "LABO_ID": "<lmis-internal-id>",
    "TAXR_GV_NM": "Somchai",
    "TAXR_FAM_NM": "Vongkhamheng"
    // ... same remaining fields as issueIndividualTin
  }
}
```

Response structure is identical to `issueIndividualTin`.

---

### POST `/mediate/TaxRIS/callAddress/ReqADD`

**UAT:** `http://202.123.182.165:8080/mediate/TaxRIS/callAddress/ReqADD`

**Request body — list provinces:**

```json
{
  "ReqADD": {
    "HASH_KEY": "6eP4tCfELd5lGAU6713+ggL5xSqmbLFkotoQWPhGORE=",
    "ACT_CD": "P",
    "ADDR_CD": ""
  }
}
```

**Request body — list districts under a province:**

```json
{
  "ReqADD": {
    "HASH_KEY": "6eP4tCfELd5lGAU6713+ggL5xSqmbLFkotoQWPhGORE=",
    "ACT_CD": "D",
    "ADDR_CD": "01"
  }
}
```

**Success response:**

```json
{
  "ResTaxRIS": {
    "Result": {
      "CD": "000",
      "MSG": "Success",
      "CNT": "18"
    },
    "Address": [
      {
        "ADDR_LVL_TP": "P",
        "ADDR_CD": "01",
        "ADDR_CD_NM": "ນະຄອນຫຼວງວຽງຈັນ"
      },
      {
        "ADDR_LVL_TP": "P",
        "ADDR_CD": "02",
        "ADDR_CD_NM": "ແຂວງຜົ້ງສາລີ"
      }
    ]
  }
}
```

---

### Implementation checklist

- [ ] Wrap the hash key in an env var (`TAXRIS_HASH_KEY`) — never hard-code
- [ ] Use the depot name (`ReqTinInfo` / `ReqADD`) as the top-level JSON key
- [ ] Parse `ResTaxRIS.Result.CD` first; treat anything other than `000` as an error
- [ ] `TinInfo` / `Address` may be absent on error responses — null-check before accessing
- [ ] `Address` is an **array** even when only one item is returned — always deserialize as list
- [ ] Schedule `issueIndividualTin` / `managePTinInformation` calls **after 6:00 PM** only

---

## Result Codes

Result codes are referenced as `Result.CD` in every response. The spec defers the full list to a
separate "Return codes and messages" document — obtain that from TaxRIS team.

| CD    | Meaning (inferred) |
|-------|--------------------|
| `000` | Success (typical pattern) |
| other | See TaxRIS return code reference |

---

## Notes

- **Timing**: TIN issuance services are called serially **after 6:00 PM**. Do not call them during
  business hours — schedule batch submission accordingly.
- **Multiplicity `1..n`** on `Address` means the response array may contain multiple address records.
- `HASH_KEY` is a fixed value per agency — store it as an environment variable, never hard-code it.
- The `PBOX_NO`, `FAX_NO`, and address sequence fields (`ADDR_SEQNO`, `UNIT_NO`) appear to be
  optional in practice (all have multiplicity `1` but may accept empty strings — confirm with TaxRIS
  team).
