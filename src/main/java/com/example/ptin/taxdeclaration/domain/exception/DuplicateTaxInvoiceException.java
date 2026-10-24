package com.example.ptin.taxdeclaration.domain.exception;

import com.example.ptin.shared.exception.DomainException;
import org.springframework.http.HttpStatus;

public class DuplicateTaxInvoiceException extends DomainException {

    public DuplicateTaxInvoiceException(String invoiceNumber) {
        super("Invoice " + invoiceNumber + " already has a tax declaration that is pending, submitted or of unknown outcome; "
                + "retry that declaration instead", HttpStatus.CONFLICT);
    }
}
