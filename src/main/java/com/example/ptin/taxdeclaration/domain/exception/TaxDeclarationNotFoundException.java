package com.example.ptin.taxdeclaration.domain.exception;

import com.example.ptin.shared.exception.DomainException;
import com.example.ptin.taxdeclaration.domain.model.TaxDeclarationId;
import org.springframework.http.HttpStatus;

public class TaxDeclarationNotFoundException extends DomainException {

    public TaxDeclarationNotFoundException(TaxDeclarationId id) {
        super("No tax declaration found with id: " + id, HttpStatus.NOT_FOUND);
    }
}
