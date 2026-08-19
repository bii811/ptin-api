package com.example.ptin.auth.application.usecase;

import com.example.ptin.auth.config.OtpProperties;
import com.example.ptin.auth.domain.exception.OtpRequestThrottledException;
import com.example.ptin.auth.domain.model.MobileNumber;
import com.example.ptin.auth.domain.model.OtpChallenge;
import com.example.ptin.auth.domain.model.OtpPurpose;
import com.example.ptin.auth.domain.port.out.OtpChallengeRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * The one place an OTP challenge is created, shared by the registration, login and password-reset
 * flows. Issuing a code always: enforces the resend cooldown and per-window rate limit, retires any
 * still-outstanding code for the same number/purpose, then persists the new one BCrypt-hashed.
 *
 * <p>Returns the plaintext code to the caller for dispatch. The caller must send it
 * <em>outside</em> this transaction so an SMS round-trip never holds a DB connection open.
 */
@Component
class OtpChallengeIssuer {

    private final OtpChallengeRepository otpChallengeRepository;
    private final OtpCodeGenerator otpCodeGenerator;
    private final PasswordEncoder passwordEncoder;
    private final OtpProperties properties;
    private final Clock clock;

    OtpChallengeIssuer(
            OtpChallengeRepository otpChallengeRepository,
            OtpCodeGenerator otpCodeGenerator,
            PasswordEncoder passwordEncoder,
            OtpProperties properties,
            Clock clock) {
        this.otpChallengeRepository = otpChallengeRepository;
        this.otpCodeGenerator = otpCodeGenerator;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
        this.clock = clock;
    }

    /** @return the plaintext code, for the caller to dispatch after this transaction commits */
    @Transactional
    public String issue(MobileNumber mobileNumber, OtpPurpose purpose) {
        Instant now = clock.instant();
        enforceRequestLimits(mobileNumber, purpose, now);
        retireOutstandingChallenges(mobileNumber, purpose, now);

        String plainCode = otpCodeGenerator.generate();
        otpChallengeRepository.save(OtpChallenge.issue(
                mobileNumber,
                purpose,
                passwordEncoder.encode(plainCode),
                now.plus(properties.ttl()),
                properties.maxAttempts()));
        return plainCode;
    }

    private void enforceRequestLimits(MobileNumber mobileNumber, OtpPurpose purpose, Instant now) {
        List<Instant> issuedInWindow = otpChallengeRepository.findIssueTimestampsSince(
                mobileNumber, purpose, now.minus(properties.requestWindow()));
        if (issuedInWindow.isEmpty()) {
            return;
        }
        if (issuedInWindow.getFirst().isAfter(now.minus(properties.resendCooldown()))) {
            throw new OtpRequestThrottledException(
                    "Please wait " + properties.resendCooldown().toSeconds() + " seconds before requesting a new code");
        }
        if (issuedInWindow.size() >= properties.maxRequestsPerWindow()) {
            throw new OtpRequestThrottledException("Too many code requests. Try again later.");
        }
    }

    /** A newly issued code supersedes the previous one immediately, rather than at its TTL. */
    private void retireOutstandingChallenges(MobileNumber mobileNumber, OtpPurpose purpose, Instant now) {
        otpChallengeRepository.findUnconsumed(mobileNumber, purpose).forEach(challenge -> {
            challenge.invalidate(now);
            otpChallengeRepository.save(challenge);
        });
    }
}
