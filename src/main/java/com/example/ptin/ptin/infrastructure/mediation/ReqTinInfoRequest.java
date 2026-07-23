package com.example.ptin.ptin.infrastructure.mediation;

import com.example.ptin.ptin.domain.model.PtinApplication;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ReqTinInfoRequest(
        @JsonProperty("LABO_ID") String laboId,
        @JsonProperty("TAXR_GV_NM") String taxrGvNm,
        @JsonProperty("TAXR_FAM_NM") String taxrFamNm,
        @JsonProperty("GND_TP") String gndTp,
        @JsonProperty("NAT_TP") String natTp,
        @JsonProperty("BDAY") String bday,
        @JsonProperty("TEL_NO") String telNo,
        @JsonProperty("HP_NO") String hpNo,
        @JsonProperty("FAX_NO") String faxNo,
        @JsonProperty("EMAIL") String email,
        @JsonProperty("IND_ID") String indId,
        @JsonProperty("IND_ID_TP") String indIdTp,
        @JsonProperty("FAMB_ISSU_PLC") String fambIssuPlc,
        @JsonProperty("ADDR_SEQNO") String addrSeqno,
        @JsonProperty("UNIT_NO") String unitNo,
        @JsonProperty("ROAD_NM") String roadNm,
        @JsonProperty("HOU_NO") String houNo,
        @JsonProperty("PBOX_NO") String pboxNo,
        @JsonProperty("PUB_OFFI_YN") String pubOffiYn,
        @JsonProperty("IND_BUSN_OPR_YN") String indBusnOprYn,
        @JsonProperty("PVT_CO_EMP_YN") String pvtCoEmpYn,
        @JsonProperty("ETC_JOB_CONT") String etcJobCont,
        @JsonProperty("DIVD_INC_YN") String divdIncYn,
        @JsonProperty("RENT_INC_YN") String rentIncYn,
        @JsonProperty("ETC_INC_CONT") String etcIncCont,
        @JsonProperty("SRL_AMT") String srlAmt,
        @JsonProperty("WORK_TIN") String workTin,
        @JsonProperty("WORK_ADDR_SEQNO") String workAddrSeqno,
        @JsonProperty("WORK_UNIT_NO") String workUnitNo,
        @JsonProperty("WORK_ROAD_NM") String workRoadNm,
        @JsonProperty("WORK_HOU_NO") String workHouNo,
        @JsonProperty("BANK_ACC_NO") String bankAccNo,
        @JsonProperty("SO_SE_NO") String soSeNo,
        @JsonProperty("HASH_KEY") String hashKey,
        @JsonProperty("SYS") String sys) {

    public static ReqTinInfoRequest from(PtinApplication application, String hashKey, String sys) {
        var personal = application.getPersonalInfo();
        var contact = application.getContactInfo();
        var address = application.getAddressInfo();
        var employment = application.getEmploymentInfo();
        var income = application.getIncomeInfo();
        var financial = application.getFinancialInfo();

        return new ReqTinInfoRequest(
                personal.laboId(),
                personal.givenName(),
                personal.familyName(),
                personal.gender(),
                personal.nationality(),
                personal.birthDay(),
                contact.telNo(),
                contact.hpNo(),
                contact.faxNo(),
                contact.email(),
                personal.individualId(),
                personal.individualIdType(),
                personal.familyBookIssuancePlace(),
                address.addrSeqNo(),
                address.unitNo(),
                address.roadNm(),
                address.houNo(),
                address.pboxNo(),
                employment.pubOffiYn(),
                employment.indBusnOprYn(),
                employment.pvtCoEmpYn(),
                employment.etcJobCont(),
                income.divdIncYn(),
                income.rentIncYn(),
                income.etcIncCont(),
                employment.srlAmt(),
                employment.workTin(),
                employment.workAddrSeqNo(),
                employment.workUnitNo(),
                employment.workRoadNm(),
                employment.workHouNo(),
                financial.bankAccNo(),
                financial.soSeNo(),
                hashKey,
                sys);
    }
}
