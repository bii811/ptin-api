package com.example.ptin.ptin.domain.exception;

import com.example.ptin.shared.exception.DomainException;
import org.springframework.http.HttpStatus;

public class PtinApplicationAlreadyExistsException extends DomainException {

    public PtinApplicationAlreadyExistsException() {
        super("You already have a PTIN application; only one application per user is allowed", HttpStatus.CONFLICT);
    }
}
