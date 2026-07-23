package com.example.ptin.auth.domain.exception;

import com.example.ptin.shared.exception.DomainException;
import org.springframework.http.HttpStatus;

public class OtpAttemptsExceededException extends DomainException {

    public OtpAttemptsExceededException() {
        super("Maximum OTP verification attempts exceeded", HttpStatus.TOO_MANY_REQUESTS);
    }
}
