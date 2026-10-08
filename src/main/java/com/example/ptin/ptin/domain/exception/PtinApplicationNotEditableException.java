package com.example.ptin.ptin.domain.exception;

import com.example.ptin.ptin.domain.model.PtinApplicationId;
import com.example.ptin.shared.exception.DomainException;
import org.springframework.http.HttpStatus;

public class PtinApplicationNotEditableException extends DomainException {

    public PtinApplicationNotEditableException(PtinApplicationId id) {
        super("PTIN application " + id + " can only be edited while pending approval or after a failed issuance",
                HttpStatus.CONFLICT);
    }
}
