package com.example.ptin.ptin.domain.model;

import com.example.ptin.shared.validation.FieldValidation;

public record TaxpayerIdentificationNumber(String value) {

    public TaxpayerIdentificationNumber {
        FieldValidation.requireMaxLength("tin", value, 12);
    }
}
