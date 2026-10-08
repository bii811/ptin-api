-- ADMIN/SUPERADMIN log in by username and have no mobile number; applicants keep mobile-only.
ALTER TABLE users ALTER COLUMN mobile_number DROP NOT NULL;
ALTER TABLE users ADD COLUMN username VARCHAR(50);
ALTER TABLE users ADD CONSTRAINT uq_users_username UNIQUE (username);
ALTER TABLE users ADD CONSTRAINT ck_users_identifier CHECK (mobile_number IS NOT NULL OR username IS NOT NULL);
