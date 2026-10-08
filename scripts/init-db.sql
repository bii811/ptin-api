-- First-time setup (dev, UAT and prod alike): seed the initial staff accounts.
--   username "superadmin" -> role SUPERADMIN
--   username "admin"      -> role ADMIN
-- Staff sign in by username + password (POST /api/v1/auth/login/password {username, password}); they have
-- no mobile number, so there is no OTP/forgot-password flow for them.
--
-- The schema is owned by Flyway and is created when the application first starts, so run this AFTER the
-- first boot, pointing psql at that environment's DB. Passwords are passed as BCrypt hashes (never
-- plaintext, never committed). Generate one with:
--
--   htpasswd -bnBC 10 "" 'the-password' | tr -d ':\n' | sed 's/^\$2y/\$2a/'
--
--   psql -h localhost -p 5433 -U ptin -d ptin \
--        -v superadmin_password_hash='$2a$10$...' -v admin_password_hash='$2a$10$...' \
--        -f scripts/init-db.sql
--
-- Either variable may be omitted to seed only the other one; at least one is required.
-- Safe to re-run: an existing username is left untouched (to change a password, update password_hash by hand).
-- Staff change their own password afterwards via POST /api/v1/auth/password.

\if :{?superadmin_password_hash}
\elif :{?admin_password_hash}
\else
  \echo 'Usage: psql ... [-v superadmin_password_hash=<bcrypt>] [-v admin_password_hash=<bcrypt>] -f scripts/init-db.sql'
  \quit
\endif

\if :{?superadmin_password_hash}
INSERT INTO users (id, username, role, status, password_hash, password_updated_at, created_at, created_by, updated_at, updated_by)
VALUES (gen_random_uuid(), 'superadmin', 'SUPERADMIN', 'ACTIVE', :'superadmin_password_hash', now(), now(), 'init-db', now(), 'init-db')
ON CONFLICT (username) DO NOTHING;
\endif

\if :{?admin_password_hash}
INSERT INTO users (id, username, role, status, password_hash, password_updated_at, created_at, created_by, updated_at, updated_by)
VALUES (gen_random_uuid(), 'admin', 'ADMIN', 'ACTIVE', :'admin_password_hash', now(), now(), 'init-db', now(), 'init-db')
ON CONFLICT (username) DO NOTHING;
\endif
