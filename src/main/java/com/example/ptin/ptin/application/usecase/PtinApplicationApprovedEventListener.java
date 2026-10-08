package com.example.ptin.ptin.application.usecase;

import com.example.ptin.ptin.domain.event.PtinApplicationApprovedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
class PtinApplicationApprovedEventListener {

    private final PtinMediationCoordinator ptinMediationCoordinator;

    PtinApplicationApprovedEventListener(PtinMediationCoordinator ptinMediationCoordinator) {
        this.ptinMediationCoordinator = ptinMediationCoordinator;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onApproved(PtinApplicationApprovedEvent event) {
        ptinMediationCoordinator.submitAndRecordOutcome(
                event.applicationId(), PtinMediationCoordinator.FUNCTION_APPROVE, event.approvedBy());;
    }
}
