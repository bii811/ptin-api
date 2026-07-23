package com.example.ptin.ptin.domain.model;

public record IncomeInfo(String divdIncYn, String rentIncYn, String etcIncCont) {

    public IncomeInfo {
        FieldValidation.requireYesNo("divdIncYn", divdIncYn);
        FieldValidation.requireYesNo("rentIncYn", rentIncYn);
        FieldValidation.requireMaxLength("etcIncCont", etcIncCont, 2000);
    }
}
