package com.example.ptin.auth.domain.exception;

import com.example.ptin.shared.exception.DomainException;
import org.springframework.http.HttpStatus;

public class AccountLockedException extends DomainException {

    public AccountLockedException() {
        super("Too many failed login attempts. Try again later or reset your password.", HttpStatus.LOCKED);
    }
}
