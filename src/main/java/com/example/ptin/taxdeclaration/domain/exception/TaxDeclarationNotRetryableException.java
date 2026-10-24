package com.example.ptin.taxdeclaration.domain.exception;

import com.example.ptin.shared.exception.DomainException;
import com.example.ptin.taxdeclaration.domain.model.TaxDeclarationId;
import com.example.ptin.taxdeclaration.domain.model.TaxDeclarationStatus;
import org.springframework.http.HttpStatus;

public class TaxDeclarationNotRetryableException extends DomainException {

    public TaxDeclarationNotRetryableException(TaxDeclarationId id, TaxDeclarationStatus status) {
        super("Tax declaration " + id + " is " + status + " and cannot be retried", HttpStatus.CONFLICT);
    }
}
