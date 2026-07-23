package com.example.ptin.ptin.domain.model;

public record PersonalInfo(
        String laboId,
        String givenName,
        String familyName,
        String gender,
        String nationality,
        String birthDay,
        String individualId,
        String individualIdType,
        String familyBookIssuancePlace) {

    public PersonalInfo {
        FieldValidation.requireMaxLength("laboId", laboId, 20);
        FieldValidation.requireMaxLength("givenName", givenName, 200);
        FieldValidation.requireMaxLength("familyName", familyName, 200);
        if (gender != null && !gender.equals("M") && !gender.equals("F")) {
            throw new IllegalArgumentException("gender must be 'M' or 'F'");
        }
        FieldValidation.requireMaxLength("nationality", nationality, 2);
        FieldValidation.requireMaxLength("birthDay", birthDay, 8);
        FieldValidation.requireMaxLength("individualId", individualId, 20);
        FieldValidation.requireMaxLength("individualIdType", individualIdType, 2);
        FieldValidation.requireMaxLength("familyBookIssuancePlace", familyBookIssuancePlace, 300);
    }
}
