# PTIN API Integration Guide

Complete reference for client apps (web/mobile) and back-office tools integrating with the PTIN API.
Authentication endpoints are covered in depth in [auth-api-integration.md](auth-api-integration.md); this
guide gives the whole-system picture and documents everything else.

- Base path: `/api/v1`
- Content type: `application/json` (UTF-8)
- Auth: stateless JWT, `Authorization: Bearer <accessToken>` on every endpoint except the public auth ones
- Environments: local `http://localhost:8080`; UAT/prod hosts are in `docs/server.txt`

## 1. Concepts

| Term | Meaning |
|---|---|
| **PTIN** | Personal Taxpayer Identification Number. Issued by TaxRIS (the tax authority system) after an authorizer approves an application. |
| **Applicant** | Role `APPLICANT`. Self-registers, completes a profile, submits PTIN applications, declares tax once a TIN is issued. |
| **Authorizer** | Role `AUTHORIZER`. Reviews, approves, rejects and retries PTIN applications. Provisioned out-of-band. |
| **Admin** | Role `ADMIN`. Can issue temporary passwords. Provisioned out-of-band. |
| **PTIN type** | `INDIVIDUAL` (ordinary individual) or `LABOR` (worker coming from the LMIS labor system, requires `laboId`). Chooses which TaxRIS service issues the TIN. |
| **TaxRIS** | External tax system. The API calls it server-side; clients never talk to it. |

### End-to-end journey

```
1. Register           POST /auth/register/otp/request → /otp/verify → /complete
2. Log in             POST /auth/login/password                      → accessToken
3. Complete profile   PUT  /profile/me                (firstName, lastName required)
4. Apply              POST /ptin/applications          → PENDING_APPROVAL
5. Authorizer decides POST /ptin/applications/{id}/approve | /reject
                         └ approve → API calls TaxRIS → ISSUED (TIN) or ISSUANCE_FAILED
6. (if failed)        POST /ptin/applications/{id}/retry   (authorizer)
7. Declare tax        POST /tax-declarations           (requires an ISSUED PTIN)
8. Log in by TIN      POST /auth/login/password {tin, password}  (works once ISSUED)
```

## 2. Conventions

### Response envelope

Every response, success or error, has the same shape:

```json
{ "success": true, "data": { }, "message": null }
```

Errors: `{ "success": false, "data": null, "message": "<reason>" }`. Branch on the HTTP status; `message` is
human-readable and **not** a stable code.

### HTTP status codes

| Status | When |
|---|---|
| 200 | OK |
| 201 | Created (`POST /ptin/applications`, `POST /tax-declarations`) |
| 400 | Bean validation failure (`message` = `field: reason, ...`) or domain validation (`IllegalArgumentException`, e.g. `gender must be 'M' or 'F'`) |
| 401 | Missing/invalid/expired token, wrong credentials |
| 403 | Authenticated but not allowed (role, or reading someone else's record) |
| 404 | Resource not found |
| 409 | State conflict (e.g. approving a non-pending application) |
| 422 | Business precondition not met (profile incomplete, no issued PTIN) |
| 423 | Account locked |
| 429 | OTP throttled |

### Data formats

| Kind | Format |
|---|---|
| IDs | UUID string |
| Timestamps | ISO-8601 UTC, e.g. `2026-10-08T04:30:00Z` |
| Dates (`birthDay`, `invoiceDate`) | `YYYYMMDD` string, e.g. `19900115` |
| Yes/No flags | `"Y"` or `"N"` |
| Amounts and counts | **Strings** (up to 28 chars for amounts, 10 for counts) — never JSON numbers |
| Mobile number | `^20\d{8}$` (e.g. `2055123456`) |
| Optional text | Omit or send `null`; empty strings are accepted |

### Pagination

Only `GET /ptin/applications/search` is paged. Query params `page` (0-based), `size` (default 20), `sort`
(default `submittedAt,desc`). Response `data`:

```json
{ "content": [ ], "page": 0, "size": 20, "totalElements": 134, "totalPages": 7, "hasNext": true, "hasPrevious": false }
```

### Roles and access

| Endpoint group | APPLICANT | AUTHORIZER | ADMIN |
|---|---|---|---|
| `/auth/*` (public ones) | yes | yes | yes |
| `/profile/me` | own | own | own |
| Submit / list-mine PTIN | yes | yes | yes |
| Get PTIN by id | own only | any | own only |
| List / search / approve / reject / retry PTIN | no (403) | yes | no |
| `/tax-declarations/*` | own | own | own |
| `/admin/users/temporary-password` | no | no | yes |

## 3. Authentication (summary)

Full details, error tables and cURL/TypeScript samples: [auth-api-integration.md](auth-api-integration.md).

| Method & path | Auth | Body → result |
|---|---|---|
| `POST /auth/register/otp/request` | public | `{mobileNumber}` → OTP sent (409 if already registered) |
| `POST /auth/register/otp/verify` | public | `{mobileNumber, otpCode}` → `registrationToken` (10 min) |
| `POST /auth/register/complete` | public | `{registrationToken, password}` → account created |
| `POST /auth/login/password` | public | `{mobileNumber \| tin, password}` (exactly one identifier) → `accessToken`, or `passwordChangeRequired` + `resetToken` |
| `POST /auth/forgot-password/otp/request` | public | `{mobileNumber}` → always generic success |
| `POST /auth/forgot-password/otp/verify` | public | `{mobileNumber, otpCode}` → `resetToken` |
| `POST /auth/forgot-password/reset` | public | `{resetToken, newPassword}` |
| `POST /auth/password` | Bearer | change password |
| `GET /auth/me` | Bearer | `{mobileNumber, role, profileComplete, tin, passwordSet}` |
| `POST /admin/users/temporary-password` | Bearer (ADMIN) | `{mobileNumber}` → `{temporaryPassword}` (shown once) |

Notes: access tokens last 24 h and are **not** revoked on password change. Lockout after 5 failed logins
(423). Password: min 8 chars, max 72 bytes; weak mixes are accepted but flagged `passwordStrength: "WEAK"`.
In UAT the OTP is echoed in the response `message` (`"OTP: 123456"`); in prod it is not.

## 4. Profile

A profile is created automatically (empty) on registration. It must be **complete** (`firstName` and
`lastName` set) before a PTIN application can be submitted.

### `GET /profile/me`

```json
{ "success": true, "data": { "firstName": "Somchai", "lastName": "Vongkhamheng", "avatarUrl": null, "complete": true }, "message": null }
```

### `PUT /profile/me`

Request: `{ "firstName": "Somchai", "lastName": "Vongkhamheng", "avatarUrl": null }`
(`firstName`, `lastName` required, non-blank). Returns the updated profile (same shape as GET).

| Status | Meaning |
|---|---|
| 400 | Blank first/last name |
| 404 | No profile for this user |

## 5. PTIN applications

Base: `/ptin/applications`

### 5.1 Status lifecycle

```
                 approve (authorizer)                 TaxRIS success
PENDING_APPROVAL ───────────────────► APPROVED ──────────────────────► ISSUED  (TIN assigned)
      │                                  │
      │ reject (authorizer)              │ TaxRIS failure
      ▼                                  ▼
   REJECTED                       ISSUANCE_FAILED ──retry──► (TaxRIS again) ► ISSUED | ISSUANCE_FAILED
```

- `approve` / `reject` only work from `PENDING_APPROVAL` (otherwise **409**).
- `retry` only works from `APPROVED` or `ISSUANCE_FAILED` (otherwise **409**).
- `REJECTED` and `ISSUED` are terminal.
- `APPROVED` is transient: the approve call triggers TaxRIS issuance before the response is returned, so the
  returned status is normally already `ISSUED` or `ISSUANCE_FAILED`.

### 5.2 Submit — `POST /ptin/applications`

Any authenticated user. Returns **201** with the created application (status `PENDING_APPROVAL`).

```json
{
  "ptinType": "INDIVIDUAL",
  "laboId": null,
  "givenName": "Somchai",
  "familyName": "Vongkhamheng",
  "gender": "M",
  "nationality": "LA",
  "birthDay": "19900115",
  "individualId": "1234567890",
  "individualIdType": "01",
  "familyBookIssuancePlace": "Vientiane",
  "telNo": null,
  "hpNo": "2055123456",
  "faxNo": null,
  "email": null,
  "addrSeqNo": null,
  "unitNo": null,
  "roadNm": null,
  "houNo": "123",
  "pboxNo": null,
  "pubOffiYn": "N",
  "indBusnOprYn": "N",
  "pvtCoEmpYn": "Y",
  "etcJobCont": null,
  "workTin": "123456789012",
  "workAddrSeqNo": null,
  "workUnitNo": null,
  "workRoadNm": null,
  "workHouNo": null,
  "srlAmt": null,
  "divdIncYn": "N",
  "rentIncYn": "N",
  "etcIncCont": null,
  "bankAccNo": null,
  "soSeNo": null
}
```

**Required:** `ptinType`, `givenName`, `familyName`, `gender`, `nationality`, `birthDay`. Everything else is
optional.

| Field | Rule |
|---|---|
| `ptinType` | `INDIVIDUAL` or `LABOR`. **`LABOR` requires a non-blank `laboId`** (400 otherwise). For `INDIVIDUAL`, `laboId` is ignored and never sent to TaxRIS. |
| `laboId` | max 20 |
| `givenName`, `familyName` | max 200 |
| `gender` | `M` or `F` |
| `nationality` | max 2 (ISO country code, e.g. `LA`) |
| `birthDay` | max 8, `YYYYMMDD` |
| `individualId` | max 20 (e.g. national ID number) |
| `individualIdType` | max 2 |
| `familyBookIssuancePlace` | max 300 |
| `telNo`, `hpNo`, `faxNo` | max 20 |
| `email` | max 200 |
| `addrSeqNo` | max 8 |
| `unitNo` | max 3 |
| `roadNm`, `houNo` | max 200 |
| `pboxNo` | max 10 |
| `pubOffiYn` (civil servant), `indBusnOprYn` (freelance), `pvtCoEmpYn` (private employee), `divdIncYn` (dividend), `rentIncYn` (rental) | `Y` or `N` |
| `etcJobCont`, `etcIncCont` | max 2000 |
| `workTin` | max 12 |
| `workAddrSeqNo` | max 8 |
| `workUnitNo` | max 3 |
| `workRoadNm`, `workHouNo` | max 200 |
| `srlAmt` | max 28 (stored only; not sent to TaxRIS) |
| `bankAccNo` | max 28 |
| `soSeNo` | max 20 |

| Status | Meaning |
|---|---|
| 400 | Validation failure (see rules above) |
| 422 | Profile incomplete — call `PUT /profile/me` first |

### 5.3 Application object

Returned by every PTIN endpoint (`data`, or items in lists/pages):

```json
{
  "id": "6f1c2e0a-...",
  "userId": "a3b9...",
  "status": "ISSUED",
  "ptinType": "INDIVIDUAL",
  "submittedAt": "2026-10-08T04:30:00Z",
  "approvedAt": "2026-10-08T05:00:00Z",
  "rejectedAt": null,
  "rejectionReason": null,
  "issuedAt": "2026-10-08T05:00:02Z",
  "failureReason": null,
  "retryCount": 0,
  "tin": "100000000001",
  "givenName": "Somchai",
  "familyName": "Vongkhamheng"
}
```

`tin` is `null` until `ISSUED`. On issuance, `givenName`/`familyName` are overwritten with the names TaxRIS
confirms. `failureReason` holds the TaxRIS (or transport) message when `ISSUANCE_FAILED`; `retryCount` counts
failed attempts.

### 5.4 Applicant endpoints

| Method & path | Result |
|---|---|
| `GET /ptin/applications/me` | `data`: array of the caller's applications |
| `GET /ptin/applications/{id}` | One application. **403** if it belongs to someone else (authorizers may read any). **404** if unknown. |

### 5.5 Authorizer endpoints

All return **403** for non-authorizers.

| Method & path | Notes |
|---|---|
| `GET /ptin/applications?status=<filter>` | Array. `status`: `all` (default), `pending`, `approved`/`approve`, `rejected`/`reject`, `retry` (= `ISSUANCE_FAILED`). Anything else → 400. Issued applications are only reachable via `all`/`search`. |
| `GET /ptin/applications/search` | Paged. Optional filters: `status` (`PENDING_APPROVAL`, `APPROVED`, `REJECTED`, `ISSUED`, `ISSUANCE_FAILED`), `tin`, `applicantName`, `submittedFrom`, `submittedTo` (ISO-8601 instants) plus `page`, `size`, `sort`. |
| `POST /ptin/applications/{id}/approve` | No body. Approves, then **synchronously** calls TaxRIS. Returns the updated application. |
| `POST /ptin/applications/{id}/reject` | Body `{ "reason": "..." }` (required, non-blank). Returns the updated application. |
| `POST /ptin/applications/{id}/retry` | No body. Re-submits to TaxRIS for `APPROVED` / `ISSUANCE_FAILED`. Returns the updated application. |

**Approve/retry latency:** the request blocks for the TaxRIS round trip (connect timeout 5 s, read timeout
10 s), so set the client timeout above ~15 s. A TaxRIS failure does **not** make the HTTP call fail: you
still get **200** with `status: "ISSUANCE_FAILED"` and `failureReason`. Check `status`, not just the HTTP code.

## 6. Tax declarations

Base: `/tax-declarations`. Requires the caller to hold an **`ISSUED`** PTIN; the TIN is taken from it
server-side (never sent by the client).

### `POST /tax-declarations` → 201

```json
{
  "invoiceNumber": "INV-2026-0001",
  "invoiceDate": "20261008",
  "buyerTin": "100000000002",
  "buyerFullName": "Buyer Co",
  "saleCount": "2",
  "supplyAmount": "200000",
  "serviceFee": "0",
  "exciseAmount": "0",
  "vatAmount": "20000",
  "saleAmount": "220000",
  "discountAmount": "0",
  "saleCancelCount": "0",
  "saleCancelAmount": "0",
  "items": [
    {
      "hsCode": "0101",
      "hsName": "Item A",
      "saleCount": "2",
      "unitSale": "pcs",
      "unitSaleAmount": "100000",
      "supplyAmount": "200000",
      "exciseAmount": "0",
      "vatAmount": "20000",
      "saleAmount": "220000"
    }
  ]
}
```

| Field | Rule |
|---|---|
| Required (header) | `invoiceNumber` (≤50), `invoiceDate` (≤8, `YYYYMMDD`), `saleCount` (≤10), `supplyAmount` (≤28), `saleAmount` (≤28), `items` (non-empty) |
| Optional (header) | `buyerTin` (≤20), `buyerFullName` (≤200), `serviceFee`, `exciseAmount`, `vatAmount`, `discountAmount` (≤28 each), `saleCancelCount` (≤10), `saleCancelAmount` (≤28) |
| Item required | `hsCode` (≤32), `hsName` (≤300), `saleCount` (≤10), `supplyAmount` (≤28), `saleAmount` (≤28) |
| Item optional | `unitSale` (≤50), `unitSaleAmount`, `exciseAmount`, `vatAmount` (≤28 each) |

The declaration is submitted to TaxRIS **synchronously** and the stored outcome is returned:

```json
{ "success": true, "data": { "id": "…", "status": "SUBMITTED", "tin": "100000000001", "invoiceNumber": "INV-2026-0001",
  "...": "...", "items": [ ], "submittedAt": "2026-10-08T05:10:00Z", "resultCode": "000", "resultMessage": "Success" }, "message": null }
```

`status` is `SUBMITTED` or `FAILED` (with `resultCode` / `resultMessage` explaining why). As with PTIN
issuance, a TaxRIS rejection still returns HTTP **201** — inspect `status`. A failed declaration is not
retried automatically; submit a new one.

| Status | Meaning |
|---|---|
| 400 | Validation failure |
| 422 | Caller has no issued PTIN |

### Read

| Method & path | Result |
|---|---|
| `GET /tax-declarations/me` | Array of the caller's declarations |
| `GET /tax-declarations/{id}` | One declaration (own only; 404 otherwise) |

## 7. How TaxRIS issuance works (what the client should expect)

The client never calls TaxRIS. On approve/retry the API:

1. Picks the TaxRIS service from `ptinType`:
   - `INDIVIDUAL` → `issueIndividualTin` (`/mediate/TaxRIS/issueIndividualTin/ReqTinInfo`)
   - `LABOR` → `managePTinInformation` (`/mediate/TaxRIS/managePTinInformation/ReqTinInfo`, adds `SYS=LMIS` and `LABO_ID`)
2. Wraps the application in the `ReqTinInfo` depot with the server-held `HASH_KEY` and POSTs it.
3. Treats the call as successful only when HTTP is 2xx, `ResTaxRIS.Result.CD == "000"` and a TIN is returned.
4. Stores the TIN (`ISSUED`) or `Result.MSG` / transport error as `failureReason` (`ISSUANCE_FAILED`).

### Field mapping (API → TaxRIS `ReqTinInfo`)

Descriptions are the "Attribute Name" from the TaxRIS interface spec. "Both" = sent to `issueIndividualTin`
and `managePTinInformation`.

| API field | TaxRIS key | Description (spec Attribute Name) | Type | Sent to |
|---|---|---|---|---|
| *(server)* | `HASH_KEY` | Hash Key — authorization value assigned per agency (fixed) | STRING(80) | Both |
| *(server)* | `SYS` | System Name — fixed `LMIS` | STRING(10) | `LABOR` only |
| `laboId` | `LABO_ID` | LABO_ID — LMIS labor id | STRING(20) | `LABOR` only |
| `givenName` | `TAXR_GV_NM` | Given Name | STRING(200) | Both |
| `familyName` | `TAXR_FAM_NM` | Family Name | STRING(200) | Both |
| `gender` | `GND_TP` | Gender (Male: `M`, Female: `F`) | STRING(1) | Both |
| `nationality` | `NAT_TP` | Nationality | STRING(2) | Both |
| `birthDay` | `BDAY` | BirthDay (`YYYYMMDD`) | STRING(8) | Both |
| `telNo` | `TEL_NO` | Telephone Number | STRING(20) | Both |
| `hpNo` | `HP_NO` | Mobilephone Number | STRING(20) | Both |
| `faxNo` | `FAX_NO` | Fax Number | STRING(20) | Both |
| `email` | `EMAIL` | Email | STRING(200) | Both |
| `individualId` | `IND_ID` | Individual Identification number of External Agency | STRING(20) | Both |
| `individualIdType` | `IND_ID_TP` | Individual Identification Type | STRING(2) | Both |
| `familyBookIssuancePlace` | `FAMB_ISSU_PLC` | Laos Family Book Issuance Place | STRING(300) | Both |
| `addrSeqNo` | `ADDR_SEQNO` | Number of address | STRING(8) | Both |
| `unitNo` | `UNIT_NO` | Number of unit which is below base address in address structure | STRING(3) | Both |
| `roadNm` | `ROAD_NM` | Road Name | STRING(200) | Both |
| `houNo` | `HOU_NO` | Address - House No | STRING(200) | Both |
| `pboxNo` | `PBOX_NO` | P.O. Box | STRING(10) | Both |
| `pubOffiYn` | `PUB_OFFI_YN` | Yes/No - Civil servants | STRING(1) | Both |
| `indBusnOprYn` | `IND_BUSN_OPR_YN` | Yes/No - Independent jobs/Freelance | STRING(1) | Both |
| `pvtCoEmpYn` | `PVT_CO_EMP_YN` | Yes/No - Private employees | STRING(1) | Both |
| `etcJobCont` | `ETC_JOB_CONT` | Other jobs | STRING(2000) | Both |
| `divdIncYn` | `DIVD_INC_YN` | Yes/No - Dividend income existence | STRING(1) | Both |
| `rentIncYn` | `RENT_INC_YN` | Yes/No - lease income | STRING(1) | Both |
| `etcIncCont` | `ETC_INC_CONT` | Contents of other jobs | STRING(2000) | Both |
| `workTin` | `WORK_TIN` | Working Company's TIN | STRING(12) | Both |
| `workAddrSeqNo` | `WORK_ADDR_SEQNO` | Working Place's Address | STRING(8) | Both |
| `workUnitNo` | `WORK_UNIT_NO` | Working Place's Unit | STRING(3) | Both |
| `workRoadNm` | `WORK_ROAD_NM` | Working Place's Road Name | STRING(200) | Both |
| `workHouNo` | `WORK_HOU_NO` | Working Place's House Number | STRING(200) | Both |
| `bankAccNo` | `BANK_ACC_NO` | Bank Account Number | STRING(28) | Both |
| `soSeNo` | `SO_SE_NO` | SOSE Card No | STRING(20) | Both |
| `srlAmt` | — | Not in the spec; stored only, never sent | — | Neither |

### Field mapping (TaxRIS `ResTaxRIS` → API)

| TaxRIS key | Description (spec Attribute Name) | Type | Used for |
|---|---|---|---|
| `Result.CD` | Result Code (see TaxRIS "Return codes and messages"; `000` = success) | STRING(3) | Success check; stored in `taxris_api_call_logs.result_code` |
| `Result.MSG` | Result Message — detail message of result | STRING(200) | `failureReason` on failure; `result_message` in the log |
| `Result.CNT` | Result Count — totally processed count (fixed to `1` for TIN services) | NUMBER(5) | Ignored |
| `TinInfo.TIN` | Taxpayer Identification Number | STRING(12) | `tin` |
| `TinInfo.TAXR_GV_NM` | Given Name (as confirmed by TaxRIS) | STRING(200) | Overwrites `givenName` |
| `TinInfo.TAXR_FAM_NM` | Family Name (as confirmed by TaxRIS) | STRING(200) | Overwrites `familyName` |

`TinInfo` is absent on error responses. For `callAddress` (service layer only, no REST endpoint yet) the
request is `ACT_CD` (Action Code: `P` province, `D` district) and `ADDR_CD` (Address Code, parent code), and the
response `Address` list carries `ADDR_LVL_TP` (Address Level), `ADDR_CD` (Address Code) and `ADDR_CD_NM`
(Address Name).

TaxRIS documentation says TIN issuance is intended for after 6:00 PM. The API does **not** enforce a time
window, so a daytime call may fail on the TaxRIS side; the authorizer can use `retry` later. Full TaxRIS
field mapping and sample payloads: [taxris/taxris_guide.md](taxris/taxris_guide.md).

### Audit trail

Every outbound TaxRIS call is recorded in the `taxris_api_call_logs` table (for operators, not exposed via
the API): `function_source` (`PTIN_APPROVE`, `PTIN_RETRY`, `TAX_DECLARATION_SUBMIT`), `reference_id` (the
application/declaration id), `triggered_by` (user id), `endpoint_url`, `request_headers`, `request_body`
(`HASH_KEY` redacted), `response_body`, `result_code`, `result_message`, `attempt_no`, `called_at`.

Support query for one application:

```sql
SELECT called_at, function_source, attempt_no, result_code, result_message
FROM taxris_api_call_logs
WHERE reference_id = '<application-uuid>'
ORDER BY called_at;
```

## 8. Error reference

| Status | Typical `message` |
|---|---|
| 400 | `givenName: must not be blank` · `gender must be 'M' or 'F'` · `laboId is required for a LABOR PTIN` · `Invalid status filter: foo` |
| 401 | `Invalid username or password` · `Invalid or expired token` |
| 403 | `Access denied` · `Not permitted to view this application` |
| 404 | `No PTIN application found with id: …` · `No tax declaration found with id: …` · `No profile found for user: …` |
| 409 | `PTIN application … is not pending approval` · `… is not in a retryable state` |
| 422 | `Profile must be completed (first name and last name) before submitting a PTIN application` · `An issued PTIN is required before declaring tax` |
| 423 | `Too many failed login attempts. Try again later or reset your password.` |
| 429 | OTP request throttled |

## 9. cURL walkthrough

```bash
BASE=http://localhost:8080/api/v1
H='Content-Type: application/json'
MOBILE=2055123456

# 1. Register (UAT echoes the OTP in "message")
curl -s $BASE/auth/register/otp/request -H "$H" -d "{\"mobileNumber\":\"$MOBILE\"}"
REG=$(curl -s $BASE/auth/register/otp/verify -H "$H" \
  -d "{\"mobileNumber\":\"$MOBILE\",\"otpCode\":\"123456\"}" | jq -r .data.token)
curl -s $BASE/auth/register/complete -H "$H" -d "{\"registrationToken\":\"$REG\",\"password\":\"Secret123\"}"

# 2. Log in
TOKEN=$(curl -s $BASE/auth/login/password -H "$H" \
  -d "{\"mobileNumber\":\"$MOBILE\",\"password\":\"Secret123\"}" | jq -r .data.accessToken)
A="Authorization: Bearer $TOKEN"

# 3. Complete profile
curl -s -X PUT $BASE/profile/me -H "$H" -H "$A" -d '{"firstName":"Somchai","lastName":"Vongkhamheng"}'

# 4. Apply (individual)
APP=$(curl -s $BASE/ptin/applications -H "$H" -H "$A" -d '{
  "ptinType":"INDIVIDUAL","givenName":"Somchai","familyName":"Vongkhamheng",
  "gender":"M","nationality":"LA","birthDay":"19900115","hpNo":"2055123456",
  "pubOffiYn":"N","indBusnOprYn":"N","pvtCoEmpYn":"Y","divdIncYn":"N","rentIncYn":"N"}' | jq -r .data.id)

# 5. Authorizer approves (use an AUTHORIZER token) — blocks until TaxRIS answers
curl -s -X POST $BASE/ptin/applications/$APP/approve -H "Authorization: Bearer $AUTHORIZER_TOKEN" | jq .data.status,.data.tin

# 6. If ISSUANCE_FAILED, retry later
curl -s -X POST $BASE/ptin/applications/$APP/retry -H "Authorization: Bearer $AUTHORIZER_TOKEN"

# 7. Declare tax (needs an ISSUED PTIN)
curl -s $BASE/tax-declarations -H "$H" -H "$A" -d '{
  "invoiceNumber":"INV-1","invoiceDate":"20261008","saleCount":"1","supplyAmount":"100000","saleAmount":"110000",
  "vatAmount":"10000","items":[{"hsCode":"0101","hsName":"Item A","saleCount":"1","supplyAmount":"100000","saleAmount":"110000"}]}'
```

Labor application — same call with LMIS data:

```json
{ "ptinType": "LABOR", "laboId": "LMIS-0001234", "givenName": "…", "familyName": "…", "gender": "F", "nationality": "LA", "birthDay": "19950301" }
```

## 10. Client code (TypeScript)

```ts
const BASE = "/api/v1";

type Envelope<T> = { success: boolean; data: T; message: string | null };

async function api<T>(method: string, path: string, token?: string, body?: unknown): Promise<T> {
  const res = await fetch(BASE + path, {
    method,
    headers: { "Content-Type": "application/json", ...(token && { Authorization: `Bearer ${token}` }) },
    body: body === undefined ? undefined : JSON.stringify(body),
    signal: AbortSignal.timeout(20_000), // approve/retry/declare wait on TaxRIS (up to ~15 s)
  });
  const json: Envelope<T> = await res.json();
  if (!json.success) throw Object.assign(new Error(json.message ?? res.statusText), { status: res.status });
  return json.data;
}

type PtinApplication = {
  id: string; status: "PENDING_APPROVAL" | "APPROVED" | "REJECTED" | "ISSUED" | "ISSUANCE_FAILED";
  ptinType: "INDIVIDUAL" | "LABOR"; tin: string | null; failureReason: string | null; retryCount: number;
};

// A 200 from approve/retry can still mean TaxRIS failed — check status.
async function approve(id: string, token: string) {
  const app = await api<PtinApplication>("POST", `/ptin/applications/${id}/approve`, token);
  if (app.status === "ISSUANCE_FAILED") console.warn("TaxRIS failed:", app.failureReason);
  return app;
}
```

## 11. Integration checklist

- [ ] Send amounts/counts as strings; dates as `YYYYMMDD`; Y/N flags as `"Y"`/`"N"`.
- [ ] Always send `ptinType`; send `laboId` only (and always) for `LABOR`.
- [ ] Ensure the profile is complete before offering "Apply for PTIN" (`GET /auth/me` → `profileComplete`).
- [ ] After approve / retry / declare, branch on the returned `status`, not only the HTTP code.
- [ ] Use client timeouts of ≥ 20 s for approve, retry and tax declaration calls.
- [ ] Treat `message` as display text; branch on HTTP status.
- [ ] Don't rely on a revoked token after password change — tokens live until `expiresAt`.
- [ ] Show `passwordStrength: "WEAK"` as a warning, not an error.
- [ ] After a PTIN reaches `ISSUED`, refresh `GET /auth/me`: `tin` is set and TIN login becomes available.
