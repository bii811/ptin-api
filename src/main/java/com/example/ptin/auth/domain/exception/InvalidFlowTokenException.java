package com.example.ptin.auth.domain.exception;

import com.example.ptin.shared.exception.DomainException;
import org.springframework.http.HttpStatus;

/** Missing, forged, expired, already-used or wrong-purpose registration / password-reset token. */
public class InvalidFlowTokenException extends DomainException {

    public InvalidFlowTokenException() {
        super("Invalid or expired token", HttpStatus.UNAUTHORIZED);
    }
}
