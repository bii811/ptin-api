package com.example.ptin.auth.domain.model;

import com.example.ptin.auth.domain.exception.PasswordPolicyViolationException;
import java.nio.charset.StandardCharsets;

/**
 * A user-supplied plaintext password, validated at construction so no unchecked string can reach the
 * hasher. Never log or serialise one — {@link #toString()} is redacted for exactly that reason.
 *
 * <p>Only length is enforced; missing complexity is reported through {@link #strength()} instead of
 * being rejected.
 */
public record RawPassword(String value) {

    public static final int MIN_LENGTH = 8;

    /**
     * BCrypt silently ignores everything past the 72nd byte, so a longer password would appear to be
     * accepted while only its prefix is actually checked. Reject rather than silently truncate.
     */
    public static final int MAX_BYTES = 72;

    public RawPassword {
        if (value == null || value.length() < MIN_LENGTH) {
            throw new PasswordPolicyViolationException("Password must be at least " + MIN_LENGTH + " characters");
        }
        if (value.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
            throw new PasswordPolicyViolationException("Password must not exceed " + MAX_BYTES + " bytes");
        }
    }

    public PasswordStrength strength() {
        boolean mixed = value.chars().anyMatch(Character::isUpperCase)
                && value.chars().anyMatch(Character::isLowerCase)
                && value.chars().anyMatch(Character::isDigit);
        return mixed ? PasswordStrength.STRONG : PasswordStrength.WEAK;
    }

    @Override
    public String toString() {
        return "RawPassword[REDACTED]";
    }
}
