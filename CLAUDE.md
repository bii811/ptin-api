# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Stack

- Java 21 (via Gradle toolchain), Gradle Kotlin DSL (`build.gradle.kts`), wrapper-only (no local Gradle install required)
- Spring Boot 4.1.0: Web MVC, Data JPA, Security, Actuator, Validation, RestClient, Flyway
- PostgreSQL (driver + Flyway migrations), Lombok, JJWT (`io.jsonwebtoken`) for JWT issuance/parsing
- Base package: `com.example.ptin`

## Commands

Always use the Gradle wrapper (`./gradlew`), not a system-installed `gradle`.

```bash
./gradlew build              # full build (compiles + runs tests)
./gradlew bootRun            # run the application locally
./gradlew test               # run all tests
./gradlew test --tests "com.example.ptin.PtinApplicationTests"   # run a single test class
./gradlew test --tests "*.PtinApplicationTests.contextLoads"     # run a single test method
./gradlew compileJava compileTestJava   # fast compile-only check without running tests
```

`PtinApplicationTests` is a `@SpringBootTest` context-load test — it connects to a real PostgreSQL
datasource. Start one locally first: `docker-compose up -d` (starts `ptin-postgres` on `localhost:5433`,
db/user/password all `ptin`).

### First-time DB setup

Flyway creates the schema on first boot. Then seed the first admin (no password; set it via
forgot-password): `psql ... -v admin_mobile=20XXXXXXXX -f scripts/init-db.sql`.

### Docker images

`Dockerfile` is a multi-stage build (Gradle build stage → `eclipse-temurin:21-jre` runtime) with a
`PROFILE` build arg that gets baked in as `SPRING_PROFILES_ACTIVE`. Use the scripts under `scripts/`
rather than invoking `docker build` directly:

```bash
./scripts/docker-build-debug.sh   # ptin:debug — PROFILE=default
./scripts/docker-build-uat.sh     # ptin:uat   — PROFILE=uat
./scripts/docker-build-prod.sh    # ptin:prod  — PROFILE=prod
```

## Configuration

`application.yaml` is the base config (env-var placeholders with dev-friendly defaults, e.g.
`${PTIN_DB_URL:jdbc:postgresql://localhost:5433/ptin}`). `application-uat.yaml` / `application-prod.yaml`
overlay only non-secret, environment-specific behavior (logging levels, `otp.expose-in-response`).
**Convention: real secrets/environment endpoints go through env-var placeholders in the base file
(injected by the deployment platform at runtime); profile YAML files are for behavioral differences
only, never for secrets.**

One auth toggle differs per environment:

- `otp.expose-in-response` (`true` by default and in `uat`, `false` in `prod`) — echo the plaintext
  OTP in the API response `message`, for environments without a real SMS gateway wired up.

The remaining OTP/password policy lives in `otp.*` and `auth.password.*` and is bound to the
`OtpProperties` / `PasswordLoginProperties` records in `auth/config` (durations are ISO/Boot duration
strings like `5m`, `60s`, `1h` — not `*-seconds` ints).

## Architecture

DDD modular monolith with a hexagonal (ports & adapters) layout repeated per bounded context. Modules
live under `com.example.ptin.<module>`: `auth`, `profile`, `ptin` (PTIN application/issuance), and
`shared` (cross-cutting). Each module follows:

```
<module>/
  domain/
    model/        aggregates, value objects (records), enums — pure Java, no Spring
    exception/     extend shared.exception.DomainException (carries an HttpStatus)
    event/         domain events for cross-module pub/sub
    port/in/       use-case interfaces + nested Command/Result records (driving ports)
    port/out/      repository/gateway interfaces (driven ports)
  application/
    usecase/       package-private @Service classes implementing port/in, depend only on port/out
  infrastructure/
    rest/          @RestController + request/response DTO records
    persistence/   @Entity + Spring Data JpaRepository + RepositoryAdapter (implements port/out) + Mapper
    otp/, acl/, mediation/, event/, config/, client/   other adapter kinds as needed
```

`shared` holds cross-cutting infra. `shared/mediation` is the single `TaxRisClient` for every TaxRIS
endpoint (`issueIndividualTin`, `managePTinInformation`, `callAddress`, plus a raw `post` used by the
tax-declaration adapter); each call is audited in `taxris_api_call_logs` (function source, reference id,
triggered by, URL, headers, body with `HASH_KEY` redacted, response, result code/message, attempt no).
A PTIN application has a `PtinType`: `INDIVIDUAL` → `issueIndividualTin`, `LABOR` (requires `laboId`) →
`managePTinInformation`. Also: `shared/exception` (DomainException,
GlobalExceptionHandler), `shared/web` (`ApiResponse<T>` envelope, CORS), `shared/identity` (`UserId`),
`shared/security` (JWT filter/provider/config), `shared/persistence` (`AuditableJpaEntity`, JPA
auditing config).

**Modules never reach into another module's `application`/`infrastructure` layers** — cross-module
calls go through the target module's `domain.port.in` interfaces only. Two patterns are used:

- **Domain events**, published via `ApplicationEventPublisher` and consumed by a
  `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` in the consuming module's
  `infrastructure/event` package (e.g. `auth`'s `UserRegisteredEvent` → `profile`'s
  `UserRegisteredEventListener` auto-creates an empty `Profile`).
- **Anti-corruption-layer adapters**: a module's `infrastructure/acl` adapter implements that module's
  own `domain/port/out` interface by calling into another module's `domain.port.in` use case (e.g.
  `ptin`'s `ProfileCompletionAdapter` implements `ProfileCompletionPort` by calling `profile`'s
  `GetProfileUseCase`).

### Transaction pattern for post-commit side effects

`PtinApplicationApprovedEventListener` (AFTER_COMMIT) invokes `PtinMediationCoordinator`, which calls
the external TIN mediation service and then persists the outcome via
`PtinApplicationTransactionalGateway`. Two non-obvious things are load-bearing here, both documented
in that gateway's Javadoc — read it before changing this flow:

- The gateway is a **separate Spring bean** (not just separate methods) so its `@Transactional`
  methods go through a real proxy — self-invocation within the same class silently skips
  `@Transactional`.
- Its methods use **`Propagation.REQUIRES_NEW`**, not the default. An AFTER_COMMIT listener runs
  before Spring unbinds the just-committed transaction's EntityManager from the thread; default
  propagation would silently participate in that already-committed, about-to-be-discarded session
  instead of opening a fresh one, and writes would never flush.
- The read, the external HTTP call, and the write are deliberately **not** wrapped in one transaction
  (`PtinMediationCoordinator` itself isn't `@Transactional`) so a DB connection doesn't sit idle for
  the duration of the outbound call.

### Auth model

Each multi-step flow is a state machine: an OTP is exchanged for a short-lived signed **flow token**
(JWT, `security.jwt.flow-token-expiry-seconds`, 10 min, `purpose` claim = `REGISTRATION` /
`PASSWORD_RESET`, no role), and only that token unlocks the last step. `JwtTokenProvider.parse`
rejects flow tokens and `parseFlowToken` rejects access tokens, so neither can stand in for the other.

- **Register** — `/register/otp/request` (409 if the number is already active; no row is written) →
  `/register/otp/verify` (returns registration token) → `/register/complete` `{registrationToken,
  password}` creates the ACTIVE `APPLICANT` and publishes `UserRegisteredEvent`. Nothing durable exists
  until `complete`, so a user who abandons after verify simply requests a fresh OTP and starts over.
- **Login** — `/login/password` takes `{mobileNumber | tin, password}` (exactly one identifier; TIN
  works once a PTIN is `ISSUED`). Unknown accounts burn a decoy BCrypt check and get the same 401 as a
  wrong password. Lockout after `auth.password.max-failed-attempts` (423).
- **Forgot password** — `/forgot-password/otp/request` always returns the generic success message
  (unknown numbers and throttled requests are swallowed) → `/forgot-password/otp/verify` (returns reset
  token) → `/forgot-password/reset` `{resetToken, newPassword}`; clears any lockout. A reset token is
  single-use: it is rejected if issued before the user's `passwordUpdatedAt`.
- **Temporary password (support fallback)** — `POST /api/v1/admin/users/temporary-password`
  `{mobileNumber}` (`@PreAuthorize("hasRole('ADMIN')")`; `ADMIN` is provisioned out-of-band like
  `AUTHORIZER`, and cannot target other admins) generates a random 12-char password, stores its hash
  with `must_change_password = true`, and returns it once for the admin to relay. Logging in with it
  returns `passwordChangeRequired: true` plus a `resetToken` instead of a session; the user spends it on
  `/forgot-password/reset`, which clears the flag.
- **Password policy** — 8 chars minimum (72 bytes max, BCrypt limit). Missing upper/lower/digit mix is
  accepted but reported as `passwordStrength: WEAK` in the response.
- `POST /api/v1/auth/password` (authenticated) changes the password; accounts that predate password
  registration have none, so they onboard through forgot-password (OTP login no longer exists).

`auth` learns about TINs through its own `TaxpayerTinLookupPort` (`infrastructure/acl/PtinTinLookupAdapter`
→ `ptin`'s `FindIssuedTinUseCase`), never by reaching into the `ptin` module.

OTP codes are generated in-memory, hashed with BCrypt before persisting (`OtpChallenge` never stores
the plaintext code), and sent via the `OtpSender` port — `LoggingOtpSenderAdapter` is a placeholder
that just logs the code; swap it for a real SMS gateway per environment via `@Profile` if/when one
exists (no `@Profile`-based adapter swapping exists yet — this would be the first). Both OTP
flows (registration, password reset) issue through the single `OtpChallengeIssuer`, which enforces the resend cooldown and
per-window rate limit and retires any still-outstanding code for the same number+purpose.

**Failed-attempt counters must be committed in their own transaction.** `OtpChallengeTransactionalGateway`
and `UserCredentialTransactionalGateway` both use `Propagation.REQUIRES_NEW` and both *return* an
outcome enum (`OtpVerificationResult` / `PasswordAuthenticationResult`) instead of throwing —
the caller invokes `ensureSuccess()` only after the gateway has committed. This is load-bearing, not
style: a failed attempt signals failure by throwing, which rolls the caller's transaction back, so an
increment made inside that transaction is discarded and the ceiling is never reached. Both gateways
also load their row `FOR UPDATE` (`lockActiveChallenge` / `lockById`) so concurrent attempts serialise
instead of losing updates.

Security is stateless (`SessionCreationPolicy.STATELESS`, no CSRF): `JwtAuthenticationFilter` reads
the `Authorization: Bearer` header, parses the JWT, and populates an `AuthenticatedPrincipal` with a
single `ROLE_<UserRole>` authority (`APPLICANT` or `AUTHORIZER`). `SecurityConfig` only permits
`/api/v1/auth/register/**`, `/api/v1/auth/login/**`, `/api/v1/auth/forgot-password/**`, and
`/actuator/health` without auth. **Role gating for authorizer-only actions
(`approve`/`reject`/`retry`/list-pending) is enforced with `@PreAuthorize("hasRole('AUTHORIZER')")` on
the application-service methods, not in the controller** — `MethodSecurityConfig` enables this.

Tokens carry `jti`/`iss` and are validated against the configured `security.jwt.issuer`.
`JwtTokenProvider.issueAccessToken` returns an `IssuedToken` so the advertised `expiresAt` cannot
drift from the token's own `exp`. Note that JWTs are **not** revoked on password change or reset —
an already-issued token stays valid until it expires.

### Persistence conventions

- Aggregate IDs are generated by the domain (e.g. `UserId.generate()`), never by Hibernate/DB
  identity — every JPA entity implements `Persistable<UUID>` with `isNew() == (createdAt == null)`
  so Spring Data chooses `persist()` over `merge()` correctly for pre-assigned identifiers.
- Soft delete: `deleted_at`/`deleted_by` columns + `@SQLRestriction("deleted_at is null")` on the
  entity, `markDeleted(...)` on `AuditableJpaEntity`. Auditing (`created_at/by`, `updated_at/by`) is
  wired via Spring Data JPA auditing (`JpaAuditingConfig`, `AuditorAwareImpl` pulling from the
  security context).
- Entities use Lombok (`@Getter` class-level; `@Setter` only on fields that legitimately mutate after
  creation — identity/creation-time fields like `userId`/`submittedAt` are deliberately left without
  a setter; `@NoArgsConstructor(access = AccessLevel.PROTECTED)` for the JPA no-arg constructor).
  All-args constructors that call the base class's `assignId(...)` stay hand-written since Lombok
  can't express that superclass interaction.
- Flyway migrations in `src/main/resources/db/migration`, `V<n>__description.sql` naming,
  `ddl-auto: validate` (schema is migration-owned, Hibernate never auto-generates DDL).

### API responses & errors

All controller responses wrap in `com.example.ptin.shared.web.ApiResponse<T>` (`success`, `data`,
`message`). `GlobalExceptionHandler` translates `DomainException` subclasses to their carried
`HttpStatus`, plus `MethodArgumentNotValidException` → 400, `AccessDeniedException` → 403,
`IllegalArgumentException` → 400 — all as `ApiResponse.failure(message)`.
