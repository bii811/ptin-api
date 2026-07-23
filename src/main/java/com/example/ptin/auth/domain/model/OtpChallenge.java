package com.example.ptin.auth.domain.model;

import com.example.ptin.auth.domain.exception.InvalidOtpException;
import com.example.ptin.auth.domain.exception.OtpAttemptsExceededException;
import com.example.ptin.auth.domain.exception.OtpExpiredException;
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

    public void verify(String candidateCode, OtpCodeMatcher matcher, Instant now) {
        if (consumedAt != null) {
            throw new InvalidOtpException();
        }
        if (now.isAfter(expiresAt)) {
            throw new OtpExpiredException();
        }
        if (attemptCount >= maxAttempts) {
            throw new OtpAttemptsExceededException();
        }
        if (!matcher.matches(candidateCode, hashedCode)) {
            attemptCount++;
            throw new InvalidOtpException();
        }
        consumedAt = now;
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
