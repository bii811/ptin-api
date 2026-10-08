package com.example.ptin.shared.mediation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Depot {@code ReqTinInfo} for issueIndividualTin and managePTinInformation. {@code hashKey} is
 * filled by {@link TaxRisClient}; {@code sys} and {@code laboId} are managePTinInformation-only
 * (omitted from the JSON when null).
 */
public record ReqTinInfo(
        // HASH_KEY - Hash Key - authorization value assigned per agency (fixed)
        @JsonProperty("HASH_KEY") String hashKey,
        // SYS - System Name - fixed 'LMIS' (managePTinInformation only)
        @JsonProperty("SYS") @JsonInclude(JsonInclude.Include.NON_NULL) String sys,
        // LABO_ID - LABO_ID - LMIS labor id (managePTinInformation only)
        @JsonProperty("LABO_ID") @JsonInclude(JsonInclude.Include.NON_NULL) String laboId,
        // TAXR_GV_NM - Given Name
        @JsonProperty("TAXR_GV_NM") String taxrGvNm,
        // TAXR_FAM_NM - Family Name
        @JsonProperty("TAXR_FAM_NM") String taxrFamNm,
        // GND_TP - Gender (Male: 'M', Female: 'F')
        @JsonProperty("GND_TP") String gndTp,
        // NAT_TP - Nationality
        @JsonProperty("NAT_TP") String natTp,
        // BDAY - BirthDay
        @JsonProperty("BDAY") String bday,
        // TEL_NO - Telephone Number
        @JsonProperty("TEL_NO") String telNo,
        // HP_NO - Mobilephone Number
        @JsonProperty("HP_NO") String hpNo,
        // FAX_NO - Fax Number
        @JsonProperty("FAX_NO") String faxNo,
        // EMAIL - Email
        @JsonProperty("EMAIL") String email,
        // IND_ID - Individual Identification number of External Agency
        @JsonProperty("IND_ID") String indId,
        // IND_ID_TP - Individual Identification Type
        @JsonProperty("IND_ID_TP") String indIdTp,
        // FAMB_ISSU_PLC - Laos Family Book Issuance Place
        @JsonProperty("FAMB_ISSU_PLC") String fambIssuPlc,
        // ADDR_SEQNO - Number of address
        @JsonProperty("ADDR_SEQNO") String addrSeqno,
        // UNIT_NO - Number of unit which is below base address in address structure
        @JsonProperty("UNIT_NO") String unitNo,
        // ROAD_NM - Road Name
        @JsonProperty("ROAD_NM") String roadNm,
        // HOU_NO - Address - House No
        @JsonProperty("HOU_NO") String houNo,
        // PBOX_NO - P.O. Box
        @JsonProperty("PBOX_NO") String pboxNo,
        // PUB_OFFI_YN - Yes/No - Civil servants
        @JsonProperty("PUB_OFFI_YN") String pubOffiYn,
        // IND_BUSN_OPR_YN - Yes/No - Independent jobs/Freelance
        @JsonProperty("IND_BUSN_OPR_YN") String indBusnOprYn,
        // PVT_CO_EMP_YN - Yes/No - Private employees
        @JsonProperty("PVT_CO_EMP_YN") String pvtCoEmpYn,
        // ETC_JOB_CONT - Other jobs
        @JsonProperty("ETC_JOB_CONT") String etcJobCont,
        // DIVD_INC_YN - Yes/No - Dividend income existence
        @JsonProperty("DIVD_INC_YN") String divdIncYn,
        // RENT_INC_YN - Yes/No - lease income
        @JsonProperty("RENT_INC_YN") String rentIncYn,
        // ETC_INC_CONT - Contents of other jobs
        @JsonProperty("ETC_INC_CONT") String etcIncCont,
        // WORK_TIN - Working Company's TIN
        @JsonProperty("WORK_TIN") String workTin,
        // WORK_ADDR_SEQNO - Working Place's Address
        @JsonProperty("WORK_ADDR_SEQNO") String workAddrSeqno,
        // WORK_UNIT_NO - Working Place's Unit
        @JsonProperty("WORK_UNIT_NO") String workUnitNo,
        // WORK_ROAD_NM - Working Place's Road Name
        @JsonProperty("WORK_ROAD_NM") String workRoadNm,
        // WORK_HOU_NO - Working Place's House Number
        @JsonProperty("WORK_HOU_NO") String workHouNo,
        // BANK_ACC_NO - Bank Account Number
        @JsonProperty("BANK_ACC_NO") String bankAccNo,
        // SO_SE_NO - SOSE Card No
        @JsonProperty("SO_SE_NO") String soSeNo) {

    ReqTinInfo withAuth(String hashKey, String sys) {
        return new ReqTinInfo(hashKey, sys, laboId, taxrGvNm, taxrFamNm, gndTp, natTp, bday, telNo, hpNo, faxNo,
                email, indId, indIdTp, fambIssuPlc, addrSeqno, unitNo, roadNm, houNo, pboxNo, pubOffiYn,
                indBusnOprYn, pvtCoEmpYn, etcJobCont, divdIncYn, rentIncYn, etcIncCont, workTin, workAddrSeqno,
                workUnitNo, workRoadNm, workHouNo, bankAccNo, soSeNo);
    }
}
