package com.example.ptin.taxdeclaration.application.usecase;

import com.example.ptin.taxdeclaration.domain.model.TaxDeclaration;
import com.example.ptin.taxdeclaration.domain.port.out.TaxDeclarationRepository;
import com.example.ptin.taxdeclaration.domain.port.out.TaxMediationClient;
import com.example.ptin.taxdeclaration.domain.port.out.TaxMediationClient.SubmissionResult;
import org.springframework.stereotype.Component;

/**
 * Sends a PENDING declaration to TaxRIS and stores the outcome. Deliberately not {@code @Transactional}: the
 * outbound call must not hold a DB connection, so the caller has already committed the PENDING row and
 * {@code save()} below opens its own short transaction.
 */
@Component
class TaxSubmissionCoordinator {

    private final TaxMediationClient taxMediationClient;
    private final TaxDeclarationRepository taxDeclarationRepository;

    TaxSubmissionCoordinator(TaxMediationClient taxMediationClient, TaxDeclarationRepository taxDeclarationRepository) {
        this.taxMediationClient = taxMediationClient;
        this.taxDeclarationRepository = taxDeclarationRepository;
    }

    TaxDeclaration submitAndRecord(TaxDeclaration declaration) {
        SubmissionResult result = taxMediationClient.submit(declaration);
        if (result.success()) {
            declaration.markSubmitted(result.resultCode(), result.resultMessage());
        } else if (result.outcomeUnknown()) {
            declaration.markUnknown(result.resultMessage());
        } else {
            declaration.markFailed(result.resultCode(), result.resultMessage());
        }
        return taxDeclarationRepository.save(declaration);
    }
}
