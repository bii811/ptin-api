package com.example.ptin.ptin.application.usecase;

import com.example.ptin.ptin.domain.port.in.RetryPtinIssuanceUseCase;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * Deliberately NOT {@code @Transactional}: it delegates straight to {@link PtinMediationCoordinator},
 * which must issue its own short-lived transactions around the external HTTP call rather than share
 * one held open by this method (see {@link PtinMediationCoordinator}).
 */
@Service
class RetryPtinIssuanceService implements RetryPtinIssuanceUseCase {

    private final PtinMediationCoordinator ptinMediationCoordinator;

    RetryPtinIssuanceService(PtinMediationCoordinator ptinMediationCoordinator) {
        this.ptinMediationCoordinator = ptinMediationCoordinator;
    }

    @Override
    @PreAuthorize("hasRole('AUTHORIZER')")
    public void retry(RetryPtinIssuanceCommand command) {
        ptinMediationCoordinator.submitAndRecordOutcome(command.applicationId());
    }
}
