package com.example.ptin.auth.domain.exception;

import com.example.ptin.shared.exception.DomainException;
import org.springframework.http.HttpStatus;

public class InvalidOtpException extends DomainException {

    public InvalidOtpException() {
        super("Invalid OTP code", HttpStatus.BAD_REQUEST);
    }
}
