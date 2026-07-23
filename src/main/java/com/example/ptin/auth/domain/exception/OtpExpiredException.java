package com.example.ptin.auth.domain.exception;

import com.example.ptin.shared.exception.DomainException;
import org.springframework.http.HttpStatus;

public class OtpExpiredException extends DomainException {

    public OtpExpiredException() {
        super("OTP code has expired", HttpStatus.BAD_REQUEST);
    }
}
