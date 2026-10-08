# Auth API Integration Guide

Integration reference for client apps (web/mobile) calling the PTIN auth endpoints.

- Base path: `/api/v1`
- Content type: `application/json`
- Auth: stateless JWT, `Authorization: Bearer <accessToken>` on protected endpoints

## Conventions

### Response envelope

Every response (success or error) uses the same shape:

```json
{ "success": true, "data": { }, "message": null }
```

Errors: `{ "success": false, "data": null, "message": "<reason>" }`. Branch on the HTTP status; `message` is human-readable and not a stable code.

### Mobile number

`^20\d{8}$` — 10 digits starting with `20` (e.g. `2055123456`). Anything else → `400`.

### Password policy

- Minimum 8 characters, maximum 72 UTF-8 bytes.
- Missing upper/lower/digit mix is **accepted**, but the response carries `passwordStrength: "WEAK"` (otherwise `"STRONG"`) so the client can warn.

### Tokens

| Token | Lifetime | Used for |
|---|---|---|
| Access token | 24h (`PTIN_JWT_EXPIRY_SECONDS`) | `Authorization: Bearer` on protected calls |
| Flow token (`registrationToken` / `resetToken`) | 10 min | Final step of register / password reset only |

Flow tokens and access tokens are not interchangeable. Access tokens are **not** revoked on password change; they live until `expiresAt`.

### Public vs protected endpoints

Public: `/auth/register/**`, `/auth/login/**`, `/auth/forgot-password/**`. Everything else needs a Bearer token.

## Flows

```
Register         otp/request ──► otp/verify ──► complete
                                  (registrationToken)

Login            login/password ──► accessToken
                       └─ passwordChangeRequired ──► forgot-password/reset (resetToken)

Forgot password  otp/request ──► otp/verify ──► reset
                                  (resetToken)
```

Nothing durable is created until `register/complete`; a user who abandons mid-flow just requests a fresh OTP.

---

## Endpoints

### 1. Request registration OTP

`POST /auth/register/otp/request`

```json
{ "mobileNumber": "2055123456" }
```

`200` — `data: null`. `message` is `"OTP: 123456"` where `otp.expose-in-response=true` (default and UAT; no SMS gateway), otherwise `"If the account exists, a code was sent"`.

| Status | Meaning |
|---|---|
| 400 | Invalid mobile number |
| 409 | Number already registered |
| 429 | Throttled (60s resend cooldown, 5 requests/hour) |

### 2. Verify registration OTP

`POST /auth/register/otp/verify`

```json
{ "mobileNumber": "2055123456", "otpCode": "123456" }
```

`200`:

```json
{ "success": true, "data": { "token": "<registrationToken>", "expiresAt": "2026-10-07T10:10:00Z" }, "message": null }
```

| Status | Meaning |
|---|---|
| 400 | Wrong code, or code expired (OTP TTL 5 min) |
| 429 | Max 5 verify attempts exceeded — request a new OTP |

### 3. Complete registration

`POST /auth/register/complete`

```json
{ "registrationToken": "<token>", "password": "Secret123" }
```

`200`: `{ "data": { "passwordStrength": "STRONG" } }`. Creates the `APPLICANT` account; **does not log in** — call login next.

| Status | Meaning |
|---|---|
| 400 | Password violates policy |
| 401 | Registration token invalid/expired |
| 409 | Number registered in the meantime |

### 4. Login

`POST /auth/login/password`

Provide **exactly one** identifier: `mobileNumber`, `tin` (max 12 chars; works once a PTIN is `ISSUED`) or `username` (staff only: `admin` / `superadmin`, no OTP flow; they change their password via `POST /auth/password`).

```json
{ "mobileNumber": "2055123456", "password": "Secret123" }
```

`200` (session):

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJ...",
    "tokenType": "Bearer",
    "expiresAt": "2026-10-08T10:00:00Z",
    "role": "APPLICANT",
    "passwordChangeRequired": false,
    "resetToken": null,
    "resetTokenExpiresAt": null
  }
}
```

`200` (admin-assigned temporary password) — **no session**; `accessToken`/`role` are `null`:

```json
{ "data": { "passwordChangeRequired": true, "resetToken": "<token>", "resetTokenExpiresAt": "..." } }
```

→ Send the user to the new-password screen and submit `resetToken` to [Reset password](#7-reset-password). Always check `passwordChangeRequired` before reading `accessToken`.

Roles: `APPLICANT`, `ADMIN`, `SUPERADMIN`.

| Status | Meaning |
|---|---|
| 400 | Validation failed (e.g. both/neither identifier) |
| 401 | Wrong credentials — **same response for unknown accounts** |
| 423 | Locked: 5 failed attempts → 15 min lock. Cleared by forgot-password reset |

### 5. Request password-reset OTP

`POST /auth/forgot-password/otp/request`

```json
{ "mobileNumber": "2055123456" }
```

Always `200` with the generic message, even for unknown or throttled numbers (anti-enumeration). Plaintext OTP is echoed in `message` only when `otp.expose-in-response=true`, and only if a code was actually issued.

### 6. Verify password-reset OTP

`POST /auth/forgot-password/otp/verify`

Request/response identical to [step 2](#2-verify-registration-otp); the returned token is the `resetToken`. Same `400` / `429` errors.

### 7. Reset password

`POST /auth/forgot-password/reset`

```json
{ "resetToken": "<token>", "newPassword": "NewSecret123" }
```

`200`: `{ "data": { "passwordStrength": "STRONG" } }`. Clears lockout and the temporary-password flag. The token is **single-use** — rejected if issued before the last password update.

| Status | Meaning |
|---|---|
| 400 | Password violates policy |
| 401 | Token invalid, expired, or already used |

> Accounts created before password registration have no password; they onboard through this flow.

### 8. Change password (authenticated)

`POST /auth/password` — requires Bearer token.

```json
{ "currentPassword": "Secret123", "newPassword": "NewSecret123" }
```

`currentPassword` is required only if the account already has a password. `200` with `data: null`.

| Status | Meaning |
|---|---|
| 400 | Policy violation |
| 401 | Missing/invalid token, or wrong `currentPassword` |

### 9. Current user

`GET /auth/me` — requires Bearer token.

```json
{
  "data": {
    "mobileNumber": "2055123456",
    "username": null,
    "role": "APPLICANT",
    "profileComplete": false,
    "tin": null,
    "passwordSet": true
  }
}
```

`tin` is `null` until a PTIN is issued. `mobileNumber` is `null` for staff; `username` is `null` for applicants. `401` when the token is missing/expired.

### 10. Assign temporary password (admin / support)

`POST /admin/users/temporary-password` — requires Bearer token with role `ADMIN` or `SUPERADMIN`. Applicants only (staff have no mobile number and are rejected).

```json
{ "mobileNumber": "2055123456" }
```

`200`: `{ "data": { "temporaryPassword": "aB3...12chars" } }` — shown **once**; the admin relays it to the user. The user's next login returns `passwordChangeRequired: true` (see [Login](#4-login)).

`403` if caller is not `ADMIN`.

---

## Common errors

| Status | Typical cause |
|---|---|
| 400 | Bean validation / policy / bad OTP |
| 401 | Missing, invalid or expired token; bad credentials |
| 403 | Authenticated but wrong role |
| 409 | Already registered |
| 423 | Account locked |
| 429 | OTP throttled / verify attempts exceeded |

## Client checklist

- Store `accessToken` + `expiresAt`; on `401` from a protected call, return to login (no refresh endpoint exists).
- Treat OTP-request responses uniformly; never infer account existence from them.
- Don't retry `otp/verify` blindly — 5 failures burn the code.
- Disable resend for 60s after a successful OTP request.
- Branch on `passwordChangeRequired` after every login.
- Show a "weak password" hint when `passwordStrength == "WEAK"`; it's not an error.

## Examples

End-to-end curl walkthroughs. Replace `BASE` for your environment; `jq` is used to pass tokens between steps.

```bash
BASE=http://localhost:8080/api/v1
H='Content-Type: application/json'
MOBILE=2055123456
```

### Register a new account

```bash
# 1. Request OTP (UAT/dev echoes the code in `message`)
curl -s $BASE/auth/register/otp/request -H "$H" -d "{\"mobileNumber\":\"$MOBILE\"}"
# {"success":true,"data":null,"message":"OTP: 482913"}

# 2. Verify OTP -> registration token
REG=$(curl -s $BASE/auth/register/otp/verify -H "$H" \
  -d "{\"mobileNumber\":\"$MOBILE\",\"otpCode\":\"482913\"}" | jq -r .data.token)

# 3. Complete with a password
curl -s $BASE/auth/register/complete -H "$H" \
  -d "{\"registrationToken\":\"$REG\",\"password\":\"Secret123\"}"
# {"success":true,"data":{"passwordStrength":"STRONG"},"message":null}
```

### Login and call a protected endpoint

```bash
TOKEN=$(curl -s $BASE/auth/login/password -H "$H" \
  -d "{\"mobileNumber\":\"$MOBILE\",\"password\":\"Secret123\"}" | jq -r .data.accessToken)

curl -s $BASE/auth/me -H "Authorization: Bearer $TOKEN"
# {"success":true,"data":{"mobileNumber":"2055123456","role":"APPLICANT","profileComplete":false,"tin":null,"passwordSet":true},"message":null}

# Login by TIN instead (after a PTIN is ISSUED)
curl -s $BASE/auth/login/password -H "$H" -d '{"tin":"1234567890","password":"Secret123"}'
```

### Forgot password

```bash
curl -s $BASE/auth/forgot-password/otp/request -H "$H" -d "{\"mobileNumber\":\"$MOBILE\"}"

RESET=$(curl -s $BASE/auth/forgot-password/otp/verify -H "$H" \
  -d "{\"mobileNumber\":\"$MOBILE\",\"otpCode\":\"731904\"}" | jq -r .data.token)

curl -s $BASE/auth/forgot-password/reset -H "$H" \
  -d "{\"resetToken\":\"$RESET\",\"newPassword\":\"NewSecret123\"}"
```

### Admin temporary password, then user login

```bash
# Admin issues the password (relay the returned value to the user)
curl -s $BASE/admin/users/temporary-password -H "$H" -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{\"mobileNumber\":\"$MOBILE\"}"
# {"success":true,"data":{"temporaryPassword":"k9Xr2mQvT4bz"},"message":null}

# User logs in with it: no session, only a reset token
curl -s $BASE/auth/login/password -H "$H" -d "{\"mobileNumber\":\"$MOBILE\",\"password\":\"k9Xr2mQvT4bz\"}"
# {"success":true,"data":{"accessToken":null,"tokenType":null,"expiresAt":null,"role":null,
#   "passwordChangeRequired":true,"resetToken":"eyJ...","resetTokenExpiresAt":"2026-10-07T10:10:00Z"},"message":null}
# -> submit resetToken to /auth/forgot-password/reset, then log in normally.
```

### Error examples

```bash
# Wrong password -> 401
{"success":false,"data":null,"message":"Invalid username or password"}

# Locked after 5 failures -> 423
{"success":false,"data":null,"message":"Too many failed login attempts. Try again later or reset your password."}

# Bad OTP -> 400
{"success":false,"data":null,"message":"Invalid OTP code"}

# Already registered -> 409
{"success":false,"data":null,"message":"User already registered for mobile number: 2055123456"}

# Expired/used flow token -> 401
{"success":false,"data":null,"message":"Invalid or expired token"}
```

### Client code (TypeScript)

```ts
const BASE = "/api/v1";

async function api<T>(path: string, body?: unknown, token?: string): Promise<T> {
  const res = await fetch(BASE + path, {
    method: body === undefined ? "GET" : "POST",
    headers: { "Content-Type": "application/json", ...(token && { Authorization: `Bearer ${token}` }) },
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  const json = await res.json();
  if (!json.success) throw Object.assign(new Error(json.message), { status: res.status });
  return json.data as T;
}

async function login(mobileNumber: string, password: string) {
  const r = await api<any>("/auth/login/password", { mobileNumber, password });
  if (r.passwordChangeRequired) return { next: "reset", resetToken: r.resetToken };
  return { next: "home", accessToken: r.accessToken };
}
```
