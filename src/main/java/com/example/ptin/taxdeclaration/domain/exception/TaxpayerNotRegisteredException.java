package com.example.ptin.taxdeclaration.domain.exception;

import com.example.ptin.shared.exception.DomainException;
import org.springframework.http.HttpStatus;

public class TaxpayerNotRegisteredException extends DomainException {

    public TaxpayerNotRegisteredException() {
        super("An issued PTIN is required before declaring tax", HttpStatus.UNPROCESSABLE_CONTENT);
    }
}
