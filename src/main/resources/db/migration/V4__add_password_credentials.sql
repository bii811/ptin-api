-- TIN + password login, available once a TIN has been issued. Phone + OTP remains the credential
-- before that, and the fallback afterwards, so every column here is nullable/defaulted.
ALTER TABLE users
    ADD COLUMN password_hash         VARCHAR(255),
    ADD COLUMN password_updated_at   TIMESTAMPTZ,
    ADD COLUMN failed_login_attempts INT NOT NULL DEFAULT 0,
    ADD COLUMN locked_until          TIMESTAMPTZ;

-- Resolves a TIN back to its owner on the unauthenticated password-login path. Not unique: a user
-- may hold more than one application row, and retries can re-land the same TIN.
CREATE INDEX idx_ptins_tin ON ptins (tin) WHERE tin IS NOT NULL;

-- Backs the OTP resend cooldown / per-window rate limit, which scans issue timestamps by
-- (mobile_number, purpose) newest-first.
CREATE INDEX idx_otp_challenges_issued_at ON otp_challenges (mobile_number, purpose, created_at DESC);
