package com.example.ptin.ptin.domain.model;

import com.example.ptin.shared.validation.FieldValidation;

public record FinancialInfo(String bankAccNo, String soSeNo) {

    public FinancialInfo {
        FieldValidation.requireMaxLength("bankAccNo", bankAccNo, 28);
        FieldValidation.requireMaxLength("soSeNo", soSeNo, 20);
    }
}
