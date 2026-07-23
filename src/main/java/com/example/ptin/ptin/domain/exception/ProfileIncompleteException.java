package com.example.ptin.ptin.domain.exception;

import com.example.ptin.shared.exception.DomainException;
import org.springframework.http.HttpStatus;

public class ProfileIncompleteException extends DomainException {

    public ProfileIncompleteException() {
        super("Profile must be completed (first name and last name) before submitting a PTIN application",
                HttpStatus.UNPROCESSABLE_CONTENT);
    }
}
