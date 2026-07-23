package com.example.ptin.auth.domain.exception;

import com.example.ptin.shared.exception.DomainException;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends DomainException {

    public UserNotFoundException(String mobileNumber) {
        super("No active user found for mobile number: " + mobileNumber, HttpStatus.NOT_FOUND);
    }
}
