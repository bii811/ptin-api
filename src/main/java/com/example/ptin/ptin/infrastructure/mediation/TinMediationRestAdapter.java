package com.example.ptin.ptin.infrastructure.mediation;

import com.example.ptin.ptin.domain.model.PtinApplication;
import com.example.ptin.ptin.domain.model.PtinType;
import com.example.ptin.ptin.domain.port.out.TinMediationClient;
import com.example.ptin.shared.identity.UserId;
import com.example.ptin.shared.mediation.CallContext;
import com.example.ptin.shared.mediation.ReqTinInfo;
import com.example.ptin.shared.mediation.ResTaxRis.TinInfo;
import com.example.ptin.shared.mediation.TaxRisClient;
import com.example.ptin.shared.mediation.TaxRisResult;
import org.springframework.stereotype.Component;

@Component
class TinMediationRestAdapter implements TinMediationClient {

    private static final String SYSTEM_TRIGGER = "SYSTEM";

    private final TaxRisClient taxRisClient;

    TinMediationRestAdapter(TaxRisClient taxRisClient) {
        this.taxRisClient = taxRisClient;
    }

    @Override
    public MediationResult submit(PtinApplication application, String functionSource, UserId triggeredBy) {
        CallContext ctx = new CallContext(
                functionSource,
                application.getId().toString(),
                triggeredBy == null ? SYSTEM_TRIGGER : triggeredBy.toString(),
                application.getRetryCount() + 1);
        boolean labor = application.getPtinType() == PtinType.LABOR;
        ReqTinInfo request = toRequest(application, labor);

        TaxRisResult<TinInfo> result = labor
                ? taxRisClient.managePTinInformation(request, ctx)
                : taxRisClient.issueIndividualTin(request, ctx);

        if (!result.success()) {
            return MediationResult.failure(result.message() != null ? result.message() : "TaxRIS call failed");
        }
        TinInfo tin = result.data();
        if (tin == null || tin.tin() == null || tin.tin().isBlank()) {
            return MediationResult.failure("TaxRIS returned success without a TIN");
        }
        return MediationResult.success(tin.tin(), tin.givenName(), tin.familyName());
    }

    // HASH_KEY and SYS are added by TaxRisClient; LABO_ID is only sent for LABOR (managePTinInformation).
    private static ReqTinInfo toRequest(PtinApplication application, boolean labor) {
        var personal = application.getPersonalInfo();
        var contact = application.getContactInfo();
        var address = application.getAddressInfo();
        var employment = application.getEmploymentInfo();
        var income = application.getIncomeInfo();
        var financial = application.getFinancialInfo();

        return new ReqTinInfo(
                null,
                null,
                labor ? personal.laboId() : null,
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
                employment.workTin(),
                employment.workAddrSeqNo(),
                employment.workUnitNo(),
                employment.workRoadNm(),
                employment.workHouNo(),
                financial.bankAccNo(),
                financial.soSeNo());
    }
}
