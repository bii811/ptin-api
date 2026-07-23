package com.example.ptin.ptin.domain.model;

public record FinancialInfo(String bankAccNo, String soSeNo) {

    public FinancialInfo {
        FieldValidation.requireMaxLength("bankAccNo", bankAccNo, 28);
        FieldValidation.requireMaxLength("soSeNo", soSeNo, 20);
    }
}
