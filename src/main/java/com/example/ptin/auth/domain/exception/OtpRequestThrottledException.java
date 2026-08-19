package com.example.ptin.auth.domain.exception;

import com.example.ptin.shared.exception.DomainException;
import org.springframework.http.HttpStatus;

/**
 * Guards the unauthenticated OTP-request endpoints against SMS bombing, unbounded challenge-row
 * growth, and (on registration) unbounded creation of placeholder user rows.
 */
public class OtpRequestThrottledException extends DomainException {

    public OtpRequestThrottledException(String message) {
        super(message, HttpStatus.TOO_MANY_REQUESTS);
    }
}
