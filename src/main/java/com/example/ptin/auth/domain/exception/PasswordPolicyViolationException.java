package com.example.ptin.auth.domain.exception;

import com.example.ptin.shared.exception.DomainException;
import org.springframework.http.HttpStatus;

public class PasswordPolicyViolationException extends DomainException {

    public PasswordPolicyViolationException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
