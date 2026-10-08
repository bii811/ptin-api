-- First-time setup (dev, UAT and prod alike): seed the initial staff accounts.
--   username "superadmin" -> role SUPERADMIN
--   username "admin"      -> role ADMIN
-- Staff sign in by username + password (POST /api/v1/auth/login/password {username, password}); they have
-- no mobile number, so there is no OTP/forgot-password flow for them.
--
-- The schema is owned by Flyway and is created when the application first starts, so run this AFTER the
-- first boot, pointing psql at that environment's DB:
--
--   psql -h localhost -p 5433 -U ptin -d ptin -f scripts/init-db.sql
--
-- Passwords are BCrypt hashes (never plaintext). The defaults below are the shared initial passwords;
-- override per environment with -v superadmin_password_hash='$2a$10$...' -v admin_password_hash='...'.
-- Staff should change them after first login via POST /api/v1/auth/password.
--
-- Safe to re-run: an existing username is left untouched (to change a password, update password_hash by hand).

\if :{?superadmin_password_hash}
\else
  \set superadmin_password_hash '$2b$10$ZbWRkKkPr6f/hWHspG4q9uPZwWfX0hQa5xCFBWyGyc0hLs0K9UmLS'
\endif
\if :{?admin_password_hash}
\else
  \set admin_password_hash '$2b$10$59D9GLq9YGKwyPf.AfGa2uc4.dFC3xNQrGP3K25dLF2Z4MHNOVpra'
\endif

INSERT INTO users (id, username, role, status, password_hash, password_updated_at, created_at, created_by, updated_at, updated_by)
VALUES (gen_random_uuid(), 'superadmin', 'SUPERADMIN', 'ACTIVE', :'superadmin_password_hash', now(), now(), 'init-db', now(), 'init-db')
ON CONFLICT (username) DO NOTHING;

INSERT INTO users (id, username, role, status, password_hash, password_updated_at, created_at, created_by, updated_at, updated_by)
VALUES (gen_random_uuid(), 'admin', 'ADMIN', 'ACTIVE', :'admin_password_hash', now(), now(), 'init-db', now(), 'init-db')
ON CONFLICT (username) DO NOTHING;
