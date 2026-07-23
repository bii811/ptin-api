package com.example.ptin.ptin.domain.model;

public record EmploymentInfo(
        String pubOffiYn,
        String indBusnOprYn,
        String pvtCoEmpYn,
        String etcJobCont,
        String workTin,
        String workAddrSeqNo,
        String workUnitNo,
        String workRoadNm,
        String workHouNo,
        String srlAmt) {

    public EmploymentInfo {
        FieldValidation.requireYesNo("pubOffiYn", pubOffiYn);
        FieldValidation.requireYesNo("indBusnOprYn", indBusnOprYn);
        FieldValidation.requireYesNo("pvtCoEmpYn", pvtCoEmpYn);
        FieldValidation.requireMaxLength("etcJobCont", etcJobCont, 2000);
        FieldValidation.requireMaxLength("workTin", workTin, 12);
        FieldValidation.requireMaxLength("workAddrSeqNo", workAddrSeqNo, 8);
        FieldValidation.requireMaxLength("workUnitNo", workUnitNo, 3);
        FieldValidation.requireMaxLength("workRoadNm", workRoadNm, 200);
        FieldValidation.requireMaxLength("workHouNo", workHouNo, 200);
        FieldValidation.requireMaxLength("srlAmt", srlAmt, 28);
    }
}
