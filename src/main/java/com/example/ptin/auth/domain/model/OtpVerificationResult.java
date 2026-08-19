package com.example.ptin.auth.domain.model;

import com.example.ptin.auth.domain.exception.InvalidOtpException;
import com.example.ptin.auth.domain.exception.OtpAttemptsExceededException;
import com.example.ptin.auth.domain.exception.OtpExpiredException;

/**
 * Outcome of an OTP verification attempt.
 *
 * <p>Verification <strong>returns</strong> this instead of throwing, because the failed-attempt
 * counter has to be committed before the caller is allowed to fail the request. Throwing from
 * inside the transaction that owns the counter would roll the increment back and make the
 * {@code max-attempts} ceiling unreachable — see
 * {@code com.example.ptin.auth.application.usecase.OtpChallengeTransactionalGateway}.
 */
public enum OtpVerificationResult {
    SUCCESS,
    NO_ACTIVE_CHALLENGE,
    ALREADY_CONSUMED,
    EXPIRED,
    CODE_MISMATCH,
    ATTEMPTS_EXCEEDED;

    /** Translates a non-success outcome into the matching domain exception. */
    public void ensureSuccess() {
        switch (this) {
            case SUCCESS -> {
                // nothing to do
            }
            case EXPIRED -> throw new OtpExpiredException();
            case ATTEMPTS_EXCEEDED -> throw new OtpAttemptsExceededException();
            // NO_ACTIVE_CHALLENGE / ALREADY_CONSUMED / CODE_MISMATCH deliberately collapse into one
            // indistinguishable error so a caller cannot probe which OTPs are outstanding.
            default -> throw new InvalidOtpException();
        }
    }
}
