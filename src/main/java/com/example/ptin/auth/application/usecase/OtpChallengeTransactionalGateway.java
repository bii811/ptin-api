package com.example.ptin.auth.application.usecase;

import com.example.ptin.auth.domain.model.MobileNumber;
import com.example.ptin.auth.domain.model.OtpChallenge;
import com.example.ptin.auth.domain.model.OtpPurpose;
import com.example.ptin.auth.domain.model.OtpVerificationResult;
import com.example.ptin.auth.domain.port.out.OtpChallengeRepository;
import java.time.Clock;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Verifies an OTP and commits the resulting attempt count in a transaction of its own.
 *
 * <p>Two things here are load-bearing; read before changing:
 *
 * <ul>
 *   <li><strong>REQUIRES_NEW.</strong> A failed verification has to increment {@code attempt_count}
 *       <em>durably</em>, but the caller signals failure by throwing, which rolls its own
 *       transaction back. Committing the counter in a nested, independent transaction is what makes
 *       the {@code max-attempts} ceiling reachable at all. (The previous {@code try/finally
 *       save(...)} inside the caller's transaction looked correct but was undone by that rollback,
 *       leaving a 6-digit code brute-forceable for its full TTL.)
 *   <li><strong>Returning instead of throwing.</strong> For the same reason this method must not
 *       throw the domain exception itself — that would roll back the very increment it just made.
 *       It returns the outcome and the caller calls {@link OtpVerificationResult#ensureSuccess()}
 *       once this transaction has committed.
 * </ul>
 *
 * <p>It is also a separate bean rather than a private method, because {@code @Transactional} is
 * silently ignored on self-invoked calls within the same class.
 */
@Component
class OtpChallengeTransactionalGateway {

    private final OtpChallengeRepository otpChallengeRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    OtpChallengeTransactionalGateway(
            OtpChallengeRepository otpChallengeRepository, PasswordEncoder passwordEncoder, Clock clock) {
        this.otpChallengeRepository = otpChallengeRepository;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OtpVerificationResult verifyAndConsume(MobileNumber mobileNumber, OtpPurpose purpose, String candidateCode) {
        Optional<OtpChallenge> active = otpChallengeRepository.lockActiveChallenge(mobileNumber, purpose);
        if (active.isEmpty()) {
            return OtpVerificationResult.NO_ACTIVE_CHALLENGE;
        }

        OtpChallenge challenge = active.get();
        OtpVerificationResult result = challenge.verify(candidateCode, passwordEncoder::matches, clock.instant());
        otpChallengeRepository.save(challenge);
        return result;
    }
}
