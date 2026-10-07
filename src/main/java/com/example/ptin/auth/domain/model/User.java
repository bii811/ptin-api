package com.example.ptin.auth.domain.model;

import com.example.ptin.shared.identity.UserId;
import java.time.Instant;

public class User {

    private final UserId id;
    private final MobileNumber mobileNumber;
    private final UserRole role;
    private UserStatus status;

    /** Null until the user opts into TIN + password login; OTP alone remains a valid credential. */
    private String passwordHash;

    private Instant passwordUpdatedAt;
    private int failedLoginAttempts;
    private Instant lockedUntil;

    /** True while the password is a support-assigned temporary one that must be replaced before login. */
    private boolean mustChangePassword;

    private User(
            UserId id,
            MobileNumber mobileNumber,
            UserRole role,
            UserStatus status,
            String passwordHash,
            Instant passwordUpdatedAt,
            int failedLoginAttempts,
            Instant lockedUntil,
            boolean mustChangePassword) {
        this.id = id;
        this.mobileNumber = mobileNumber;
        this.role = role;
        this.status = status;
        this.passwordHash = passwordHash;
        this.passwordUpdatedAt = passwordUpdatedAt;
        this.failedLoginAttempts = failedLoginAttempts;
        this.lockedUntil = lockedUntil;
        this.mustChangePassword = mustChangePassword;
    }

    public static User register(MobileNumber mobileNumber) {
        // Public self-registration can only ever create an APPLICANT; AUTHORIZER accounts are provisioned out-of-band.
        return new User(
                UserId.generate(), mobileNumber, UserRole.APPLICANT, UserStatus.PENDING_VERIFICATION, null, null, 0, null, false);
    }

    public static User reconstitute(
            UserId id,
            MobileNumber mobileNumber,
            UserRole role,
            UserStatus status,
            String passwordHash,
            Instant passwordUpdatedAt,
            int failedLoginAttempts,
            Instant lockedUntil,
            boolean mustChangePassword) {
        return new User(
                id,
                mobileNumber,
                role,
                status,
                passwordHash,
                passwordUpdatedAt,
                failedLoginAttempts,
                lockedUntil,
                mustChangePassword);
    }

    /** Idempotent: re-verifying an already-active account is a no-op, not an error. */
    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    /**
     * Installs a new password hash and clears any standing lockout, so a user who reset their
     * password after being locked out can log in immediately.
     */
    public void changePassword(String newPasswordHash, Instant now) {
        if (newPasswordHash == null || newPasswordHash.isBlank()) {
            throw new IllegalArgumentException("newPasswordHash must not be blank");
        }
        this.passwordHash = newPasswordHash;
        this.passwordUpdatedAt = now;
        this.mustChangePassword = false;
        clearLockout();
    }

    /**
     * Support-assigned fallback for a user who cannot receive an OTP. Logging in with it yields a
     * password-reset token instead of a session, so the user must choose their own password.
     */
    public void assignTemporaryPassword(String temporaryPasswordHash, Instant now) {
        changePassword(temporaryPasswordHash, now);
        this.mustChangePassword = true;
    }

    /**
     * Checks {@code rawPassword} and records the attempt against the lockout counters. Never throws:
     * the caller must persist this user before mapping the result to a response, otherwise the
     * rollback discards the increment and the lockout never triggers.
     */
    public PasswordAuthenticationResult authenticate(
            String rawPassword, SecretMatcher matcher, Instant now, LockoutPolicy policy) {
        expireLockoutIfElapsed(now);

        if (status != UserStatus.ACTIVE) {
            return PasswordAuthenticationResult.ACCOUNT_INACTIVE;
        }
        if (isLockedAt(now)) {
            return PasswordAuthenticationResult.ACCOUNT_LOCKED;
        }
        if (passwordHash == null) {
            return PasswordAuthenticationResult.NO_PASSWORD_SET;
        }
        if (!matcher.matches(rawPassword, passwordHash)) {
            failedLoginAttempts++;
            if (failedLoginAttempts >= policy.maxFailedAttempts()) {
                lockedUntil = now.plus(policy.lockDuration());
                failedLoginAttempts = 0;
                return PasswordAuthenticationResult.ACCOUNT_LOCKED;
            }
            return PasswordAuthenticationResult.BAD_CREDENTIALS;
        }

        clearLockout();
        return PasswordAuthenticationResult.SUCCESS;
    }

    /** Verifies the current password without touching the lockout counters (re-authentication). */
    public boolean matchesCurrentPassword(String rawPassword, SecretMatcher matcher) {
        return passwordHash != null && matcher.matches(rawPassword, passwordHash);
    }

    public boolean isLockedAt(Instant now) {
        return lockedUntil != null && now.isBefore(lockedUntil);
    }

    private void expireLockoutIfElapsed(Instant now) {
        if (lockedUntil != null && !now.isBefore(lockedUntil)) {
            clearLockout();
        }
    }

    private void clearLockout() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }

    public UserId getId() {
        return id;
    }

    public MobileNumber getMobileNumber() {
        return mobileNumber;
    }

    public UserRole getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Instant getPasswordUpdatedAt() {
        return passwordUpdatedAt;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    public boolean hasPassword() {
        return passwordHash != null;
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }
}
