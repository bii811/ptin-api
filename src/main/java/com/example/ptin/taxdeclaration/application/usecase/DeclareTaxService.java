package com.example.ptin.taxdeclaration.application.usecase;

import com.example.ptin.taxdeclaration.domain.exception.TaxpayerNotRegisteredException;
import com.example.ptin.taxdeclaration.domain.model.TaxDeclaration;
import com.example.ptin.taxdeclaration.domain.model.TaxDeclarationId;
import com.example.ptin.taxdeclaration.domain.port.in.DeclareTaxUseCase;
import com.example.ptin.taxdeclaration.domain.port.out.TaxDeclarationRepository;
import com.example.ptin.taxdeclaration.domain.port.out.TaxMediationClient;
import com.example.ptin.taxdeclaration.domain.port.out.TaxMediationClient.SubmissionResult;
import com.example.ptin.taxdeclaration.domain.port.out.TaxpayerTinPort;
import org.springframework.stereotype.Service;

/**
 * Deliberately not {@code @Transactional}: the external mediation call sits between the domain
 * construction and the single closing {@code repository.save()}, and {@code save()} already opens
 * its own short transaction (see {@link com.example.ptin.ptin.application.usecase.PtinApplicationTransactionalGateway}
 * for the fuller rationale on why a DB connection shouldn't sit idle across an outbound HTTP call).
 */
@Service
class DeclareTaxService implements DeclareTaxUseCase {

    private final TaxpayerTinPort taxpayerTinPort;
    private final TaxMediationClient taxMediationClient;
    private final TaxDeclarationRepository taxDeclarationRepository;

    DeclareTaxService(
            TaxpayerTinPort taxpayerTinPort,
            TaxMediationClient taxMediationClient,
            TaxDeclarationRepository taxDeclarationRepository) {
        this.taxpayerTinPort = taxpayerTinPort;
        this.taxMediationClient = taxMediationClient;
        this.taxDeclarationRepository = taxDeclarationRepository;
    }

    @Override
    public TaxDeclarationId declare(DeclareTaxCommand command) {
        String tin = taxpayerTinPort.findIssuedTin(command.userId()).orElseThrow(TaxpayerNotRegisteredException::new);

        TaxDeclaration declaration = TaxDeclaration.declare(
                command.userId(),
                tin,
                command.invoiceNumber(),
                command.invoiceDate(),
                command.buyerTin(),
                command.buyerFullName(),
                command.saleCount(),
                command.supplyAmount(),
                command.serviceFee(),
                command.exciseAmount(),
                command.vatAmount(),
                command.saleAmount(),
                command.discountAmount(),
                command.saleCancelCount(),
                command.saleCancelAmount(),
                command.items());

        SubmissionResult result = taxMediationClient.submit(declaration);
        if (result.success()) {
            declaration.markSubmitted(result.resultCode(), result.resultMessage());
        } else {
            declaration.markFailed(result.resultCode(), result.resultMessage());
        }

        return taxDeclarationRepository.save(declaration).getId();
    }
}
