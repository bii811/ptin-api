package com.example.ptin.auth.domain.model;

import java.time.Instant;

public class OtpChallenge {

    private final OtpChallengeId id;
    private final MobileNumber mobileNumber;
    private final OtpPurpose purpose;
    private final String hashedCode;
    private final Instant expiresAt;
    private final int maxAttempts;
    private int attemptCount;
    private Instant consumedAt;

    private OtpChallenge(
            OtpChallengeId id,
            MobileNumber mobileNumber,
            OtpPurpose purpose,
            String hashedCode,
            Instant expiresAt,
            int maxAttempts,
            int attemptCount,
            Instant consumedAt) {
        this.id = id;
        this.mobileNumber = mobileNumber;
        this.purpose = purpose;
        this.hashedCode = hashedCode;
        this.expiresAt = expiresAt;
        this.maxAttempts = maxAttempts;
        this.attemptCount = attemptCount;
        this.consumedAt = consumedAt;
    }

    public static OtpChallenge issue(
            MobileNumber mobileNumber, OtpPurpose purpose, String hashedCode, Instant expiresAt, int maxAttempts) {
        return new OtpChallenge(
                OtpChallengeId.generate(), mobileNumber, purpose, hashedCode, expiresAt, maxAttempts, 0, null);
    }

    public static OtpChallenge reconstitute(
            OtpChallengeId id,
            MobileNumber mobileNumber,
            OtpPurpose purpose,
            String hashedCode,
            Instant expiresAt,
            int maxAttempts,
            int attemptCount,
            Instant consumedAt) {
        return new OtpChallenge(id, mobileNumber, purpose, hashedCode, expiresAt, maxAttempts, attemptCount, consumedAt);
    }

    /**
     * Spends one attempt against this challenge and reports the outcome. Never throws: the caller
     * must persist the mutated attempt count (and {@code consumedAt}) before turning a failure into
     * an error response, otherwise the rollback wipes the counter and the ceiling never bites.
     */
    public OtpVerificationResult verify(String candidateCode, SecretMatcher matcher, Instant now) {
        if (consumedAt != null) {
            return OtpVerificationResult.ALREADY_CONSUMED;
        }
        if (!now.isBefore(expiresAt)) {
            return OtpVerificationResult.EXPIRED;
        }
        if (attemptCount >= maxAttempts) {
            return OtpVerificationResult.ATTEMPTS_EXCEEDED;
        }

        attemptCount++;
        if (!matcher.matches(candidateCode, hashedCode)) {
            return attemptCount >= maxAttempts
                    ? OtpVerificationResult.ATTEMPTS_EXCEEDED
                    : OtpVerificationResult.CODE_MISMATCH;
        }
        consumedAt = now;
        return OtpVerificationResult.SUCCESS;
    }

    /**
     * Burns an outstanding challenge without spending an attempt, so that issuing a replacement code
     * immediately retires the previous one instead of leaving it valid until its TTL runs out.
     */
    public void invalidate(Instant now) {
        if (consumedAt == null) {
            consumedAt = now;
        }
    }

    public OtpChallengeId getId() {
        return id;
    }

    public MobileNumber getMobileNumber() {
        return mobileNumber;
    }

    public OtpPurpose getPurpose() {
        return purpose;
    }

    public String getHashedCode() {
        return hashedCode;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public Instant getConsumedAt() {
        return consumedAt;
    }

    public boolean isConsumed() {
        return consumedAt != null;
    }
}
