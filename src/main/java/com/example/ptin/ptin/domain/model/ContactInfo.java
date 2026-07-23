package com.example.ptin.ptin.domain.model;

public record ContactInfo(String telNo, String hpNo, String faxNo, String email) {

    public ContactInfo {
        FieldValidation.requireMaxLength("telNo", telNo, 20);
        FieldValidation.requireMaxLength("hpNo", hpNo, 20);
        FieldValidation.requireMaxLength("faxNo", faxNo, 20);
        FieldValidation.requireMaxLength("email", email, 200);
    }
}
