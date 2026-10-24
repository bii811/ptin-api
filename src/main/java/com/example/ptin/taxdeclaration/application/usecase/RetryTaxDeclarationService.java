package com.example.ptin.taxdeclaration.application.usecase;

import com.example.ptin.shared.identity.UserId;
import com.example.ptin.taxdeclaration.domain.model.TaxDeclaration;
import com.example.ptin.taxdeclaration.domain.model.TaxDeclarationId;
import com.example.ptin.taxdeclaration.domain.port.in.RetryTaxDeclarationUseCase;
import org.springframework.stereotype.Service;

/** Deliberately not {@code @Transactional}: see {@link TaxSubmissionCoordinator}. */
@Service
class RetryTaxDeclarationService implements RetryTaxDeclarationUseCase {

    private final TaxDeclarationTransactionalGateway gateway;
    private final TaxSubmissionCoordinator coordinator;

    RetryTaxDeclarationService(TaxDeclarationTransactionalGateway gateway, TaxSubmissionCoordinator coordinator) {
        this.gateway = gateway;
        this.coordinator = coordinator;
    }

    @Override
    public TaxDeclaration retry(TaxDeclarationId id, UserId requester) {
        return coordinator.submitAndRecord(gateway.claimForRetry(id, requester));
    }
}
