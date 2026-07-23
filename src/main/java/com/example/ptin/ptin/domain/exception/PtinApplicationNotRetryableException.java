package com.example.ptin.ptin.domain.exception;

import com.example.ptin.ptin.domain.model.PtinApplicationId;
import com.example.ptin.shared.exception.DomainException;
import org.springframework.http.HttpStatus;

public class PtinApplicationNotRetryableException extends DomainException {

    public PtinApplicationNotRetryableException(PtinApplicationId id) {
        super("PTIN application " + id + " is not in a retryable state", HttpStatus.CONFLICT);
    }
}
