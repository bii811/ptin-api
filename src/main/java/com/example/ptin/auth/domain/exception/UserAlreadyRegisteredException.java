package com.example.ptin.auth.domain.exception;

import com.example.ptin.shared.exception.DomainException;
import org.springframework.http.HttpStatus;

public class UserAlreadyRegisteredException extends DomainException {

    public UserAlreadyRegisteredException(String mobileNumber) {
        super("User already registered for mobile number: " + mobileNumber, HttpStatus.CONFLICT);
    }
}
