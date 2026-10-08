package com.example.ptin.ptin.infrastructure.rest.request;

import com.example.ptin.ptin.domain.model.AddressInfo;
import com.example.ptin.ptin.domain.model.ContactInfo;
import com.example.ptin.ptin.domain.model.EmploymentInfo;
import com.example.ptin.ptin.domain.model.FinancialInfo;
import com.example.ptin.ptin.domain.model.IncomeInfo;
import com.example.ptin.ptin.domain.model.PersonalInfo;
import com.example.ptin.ptin.domain.model.PtinType;
import com.example.ptin.ptin.domain.port.in.SubmitPtinApplicationUseCase.SubmitPtinApplicationCommand;
import com.example.ptin.shared.identity.UserId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubmitPtinApplicationRequest(
        @NotNull PtinType ptinType,
        String laboId,
        @NotBlank String givenName,
        @NotBlank String familyName,
        @NotBlank String gender,
        @NotBlank String nationality,
        @NotBlank String birthDay,
        String individualId,
        String individualIdType,
        String familyBookIssuancePlace,
        String telNo,
        String hpNo,
        String faxNo,
        String email,
        String addrSeqNo,
        String unitNo,
        String roadNm,
        String houNo,
        String pboxNo,
        String pubOffiYn,
        String indBusnOprYn,
        String pvtCoEmpYn,
        String etcJobCont,
        String workTin,
        String workAddrSeqNo,
        String workUnitNo,
        String workRoadNm,
        String workHouNo,
        String srlAmt,
        String divdIncYn,
        String rentIncYn,
        String etcIncCont,
        String bankAccNo,
        String soSeNo) {

    public SubmitPtinApplicationCommand toCommand(UserId userId) {
        return new SubmitPtinApplicationCommand(
                userId,
                ptinType,
                new PersonalInfo(
                        laboId,
                        givenName,
                        familyName,
                        gender,
                        nationality,
                        birthDay,
                        individualId,
                        individualIdType,
                        familyBookIssuancePlace),
                new ContactInfo(telNo, hpNo, faxNo, email),
                new AddressInfo(addrSeqNo, unitNo, roadNm, houNo, pboxNo),
                new EmploymentInfo(
                        pubOffiYn,
                        indBusnOprYn,
                        pvtCoEmpYn,
                        etcJobCont,
                        workTin,
                        workAddrSeqNo,
                        workUnitNo,
                        workRoadNm,
                        workHouNo,
                        srlAmt),
                new IncomeInfo(divdIncYn, rentIncYn, etcIncCont),
                new FinancialInfo(bankAccNo, soSeNo));
    }
}
