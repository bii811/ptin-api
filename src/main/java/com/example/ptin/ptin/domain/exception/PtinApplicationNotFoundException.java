package com.example.ptin.ptin.domain.exception;

import com.example.ptin.ptin.domain.model.PtinApplicationId;
import com.example.ptin.shared.exception.DomainException;
import org.springframework.http.HttpStatus;

public class PtinApplicationNotFoundException extends DomainException {

    public PtinApplicationNotFoundException(PtinApplicationId id) {
        super("No PTIN application found with id: " + id, HttpStatus.NOT_FOUND);
    }
}
