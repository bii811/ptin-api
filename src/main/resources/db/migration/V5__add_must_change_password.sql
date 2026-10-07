-- Set when support assigns a temporary password; the user must replace it before getting a session.
ALTER TABLE users
    ADD COLUMN must_change_password BOOLEAN NOT NULL DEFAULT FALSE;
