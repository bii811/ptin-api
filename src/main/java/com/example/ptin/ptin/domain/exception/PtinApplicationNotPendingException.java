package com.example.ptin.ptin.domain.exception;

import com.example.ptin.ptin.domain.model.PtinApplicationId;
import com.example.ptin.shared.exception.DomainException;
import org.springframework.http.HttpStatus;

public class PtinApplicationNotPendingException extends DomainException {

    public PtinApplicationNotPendingException(PtinApplicationId id) {
        super("PTIN application " + id + " is not pending approval", HttpStatus.CONFLICT);
    }
}
