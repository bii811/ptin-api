package com.example.ptin.ptin.domain.model;

public record AddressInfo(String addrSeqNo, String unitNo, String roadNm, String houNo, String pboxNo) {

    public AddressInfo {
        FieldValidation.requireMaxLength("addrSeqNo", addrSeqNo, 8);
        FieldValidation.requireMaxLength("unitNo", unitNo, 3);
        FieldValidation.requireMaxLength("roadNm", roadNm, 200);
        FieldValidation.requireMaxLength("houNo", houNo, 200);
        FieldValidation.requireMaxLength("pboxNo", pboxNo, 10);
    }
}
