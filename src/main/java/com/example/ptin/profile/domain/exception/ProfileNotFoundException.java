package com.example.ptin.profile.domain.exception;

import com.example.ptin.shared.exception.DomainException;
import com.example.ptin.shared.identity.UserId;
import org.springframework.http.HttpStatus;

public class ProfileNotFoundException extends DomainException {

    public ProfileNotFoundException(UserId userId) {
        super("No profile found for user: " + userId, HttpStatus.NOT_FOUND);
    }
}
