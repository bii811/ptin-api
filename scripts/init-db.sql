-- First-time setup: seed the initial ADMIN account.
--
-- The schema itself is owned by Flyway (src/main/resources/db/migration) and is created when the
-- application first starts, so run this AFTER the first boot:
--
--   psql -h localhost -p 5433 -U ptin -d ptin -v admin_mobile=20XXXXXXXX -f scripts/init-db.sql
--
-- The admin has no password. Set one through the normal flow:
--   POST /api/v1/auth/forgot-password/otp/request  {mobileNumber}
--   POST /api/v1/auth/forgot-password/otp/verify   {mobileNumber, otpCode}
--   POST /api/v1/auth/forgot-password/reset        {resetToken, newPassword}
-- then log in via POST /api/v1/auth/login/password.
--
-- Safe to re-run: an existing user with that number is left untouched (promote it by hand if intended).
-- AUTHORIZER accounts are provisioned the same way, with role = 'AUTHORIZER'.

\if :{?admin_mobile}
\else
  \echo 'Usage: psql ... -v admin_mobile=20XXXXXXXX -f scripts/init-db.sql'
  \quit
\endif

INSERT INTO users (id, mobile_number, role, status, created_at, created_by, updated_at, updated_by)
VALUES (gen_random_uuid(), :'admin_mobile', 'ADMIN', 'ACTIVE', now(), 'init-db', now(), 'init-db')
ON CONFLICT (mobile_number) DO NOTHING;
