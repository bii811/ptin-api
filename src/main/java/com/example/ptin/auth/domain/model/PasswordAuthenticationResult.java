package com.example.ptin.auth.domain.model;

import com.example.ptin.auth.domain.exception.AccountLockedException;
import com.example.ptin.auth.domain.exception.InvalidCredentialsException;

/**
 * Outcome of a TIN + password authentication attempt. Returned rather than thrown for the same
 * reason as {@link OtpVerificationResult}: the failed-attempt counter must commit before the request
 * is failed.
 */
public enum PasswordAuthenticationResult {
    SUCCESS,
    BAD_CREDENTIALS,
    NO_PASSWORD_SET,
    ACCOUNT_INACTIVE,
    ACCOUNT_LOCKED;

    public void ensureSuccess() {
        switch (this) {
            case SUCCESS -> {
                // nothing to do
            }
            case ACCOUNT_LOCKED -> throw new AccountLockedException();
            // BAD_CREDENTIALS / NO_PASSWORD_SET / ACCOUNT_INACTIVE all surface as one generic 401 so
            // the login endpoint cannot be used to enumerate which TINs exist or have a password.
            default -> throw new InvalidCredentialsException();
        }
    }
}
