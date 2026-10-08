package com.example.ptin.ptin.application.usecase;

import com.example.ptin.ptin.domain.model.PtinApplication;
import com.example.ptin.ptin.domain.model.PtinApplicationId;
import com.example.ptin.ptin.domain.model.TaxpayerIdentificationNumber;
import com.example.ptin.ptin.domain.port.out.TinMediationClient;
import com.example.ptin.ptin.domain.port.out.TinMediationClient.MediationResult;
import com.example.ptin.shared.identity.UserId;
import org.springframework.stereotype.Component;

/**
 * Internal collaborator (not a use case port) shared by the post-approval flow and manual retry.
 * Deliberately not {@code @Transactional} at this level: the read, the external HTTP call, and the
 * write must NOT share one transaction, or a DB connection would sit idle for the duration of the
 * outbound call. {@link PtinApplicationTransactionalGateway} gives each side its own short-lived,
 * always-fresh transaction instead (see its Javadoc for why REQUIRES_NEW is required there, not
 * just tidiness).
 */
@Component
class PtinMediationCoordinator {

    static final String FUNCTION_APPROVE = "PTIN_APPROVE";
    static final String FUNCTION_RETRY = "PTIN_RETRY";

    private final PtinApplicationTransactionalGateway transactionalGateway;
    private final TinMediationClient tinMediationClient;

    PtinMediationCoordinator(PtinApplicationTransactionalGateway transactionalGateway, TinMediationClient tinMediationClient) {
        this.transactionalGateway = transactionalGateway;
        this.tinMediationClient = tinMediationClient;
    }

    void submitAndRecordOutcome(PtinApplicationId id, String functionSource, UserId triggeredBy) {
        PtinApplication application = transactionalGateway.loadOrThrow(id);

        MediationResult result = tinMediationClient.submit(application, functionSource, triggeredBy);

        if (result.success()) {
            application.markIssued(
                    new TaxpayerIdentificationNumber(result.tin()),
                    result.confirmedGivenName(),
                    result.confirmedFamilyName());
        } else {
            application.markIssuanceFailed(result.errorMessage());
        }
        transactionalGateway.save(application);
    }
}
