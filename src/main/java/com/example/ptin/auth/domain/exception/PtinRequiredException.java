package com.example.ptin.auth.domain.exception;

import com.example.ptin.shared.exception.DomainException;
import org.springframework.http.HttpStatus;

/**
 * Password login is keyed on the TIN, so a password can only be set once one has been issued. Until
 * then phone + OTP is the account's only credential.
 */
public class PtinRequiredException extends DomainException {

    public PtinRequiredException() {
        super("A password can only be set once a TIN has been issued for this account", HttpStatus.CONFLICT);
    }
}
