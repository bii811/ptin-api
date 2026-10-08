package com.example.ptin.ptin.infrastructure.rest.request;

import com.example.ptin.ptin.domain.model.AddressInfo;
import com.example.ptin.ptin.domain.model.ContactInfo;
import com.example.ptin.ptin.domain.model.EmploymentInfo;
import com.example.ptin.ptin.domain.model.FinancialInfo;
import com.example.ptin.ptin.domain.model.IncomeInfo;
import com.example.ptin.ptin.domain.model.PersonalInfo;
import com.example.ptin.ptin.domain.model.PtinApplicationId;
import com.example.ptin.ptin.domain.model.PtinType;
import com.example.ptin.ptin.domain.port.in.SubmitPtinApplicationUseCase.SubmitPtinApplicationCommand;
import com.example.ptin.ptin.domain.port.in.UpdatePtinApplicationUseCase.UpdatePtinApplicationCommand;
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
                userId, ptinType, personalInfo(), contactInfo(), addressInfo(), employmentInfo(), incomeInfo(),
                financialInfo());
    }

    public UpdatePtinApplicationCommand toUpdateCommand(PtinApplicationId id) {
        return new UpdatePtinApplicationCommand(
                id, ptinType, personalInfo(), contactInfo(), addressInfo(), employmentInfo(), incomeInfo(),
                financialInfo());
    }

    private PersonalInfo personalInfo() {
        return new PersonalInfo(
                laboId, givenName, familyName, gender, nationality, birthDay, individualId, individualIdType,
                familyBookIssuancePlace);
    }

    private ContactInfo contactInfo() {
        return new ContactInfo(telNo, hpNo, faxNo, email);
    }

    private AddressInfo addressInfo() {
        return new AddressInfo(addrSeqNo, unitNo, roadNm, houNo, pboxNo);
    }

    private EmploymentInfo employmentInfo() {
        return new EmploymentInfo(
                pubOffiYn, indBusnOprYn, pvtCoEmpYn, etcJobCont, workTin, workAddrSeqNo, workUnitNo, workRoadNm,
                workHouNo, srlAmt);
    }

    private IncomeInfo incomeInfo() {
        return new IncomeInfo(divdIncYn, rentIncYn, etcIncCont);
    }

    private FinancialInfo financialInfo() {
        return new FinancialInfo(bankAccNo, soSeNo);
    }
}
