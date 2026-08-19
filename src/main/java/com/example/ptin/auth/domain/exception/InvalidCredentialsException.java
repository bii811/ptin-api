package com.example.ptin.auth.domain.exception;

import com.example.ptin.shared.exception.DomainException;
import org.springframework.http.HttpStatus;

/**
 * Deliberately generic: unknown TIN, no password set, wrong password and inactive account all raise
 * this same error so the login endpoint reveals nothing about which TINs exist.
 */
public class InvalidCredentialsException extends DomainException {

    public InvalidCredentialsException() {
        super("Invalid TIN or password", HttpStatus.UNAUTHORIZED);
    }
}
