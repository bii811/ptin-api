package com.example.ptin.ptin.domain.model;

public record TaxpayerIdentificationNumber(String value) {

    public TaxpayerIdentificationNumber {
        FieldValidation.requireMaxLength("tin", value, 12);
    }
}
