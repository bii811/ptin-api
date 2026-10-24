package com.example.ptin.taxdeclaration.application.usecase;

import com.example.ptin.taxdeclaration.domain.exception.DuplicateTaxInvoiceException;
import com.example.ptin.taxdeclaration.domain.exception.TaxpayerNotRegisteredException;
import com.example.ptin.taxdeclaration.domain.model.TaxDeclaration;
import com.example.ptin.taxdeclaration.domain.model.TaxDeclarationId;
import com.example.ptin.taxdeclaration.domain.port.in.DeclareTaxUseCase;
import com.example.ptin.taxdeclaration.domain.port.out.TaxDeclarationRepository;
import com.example.ptin.taxdeclaration.domain.port.out.TaxpayerTinPort;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/**
 * Deliberately not {@code @Transactional}: the PENDING row is committed first (so a crash can never leave TaxRIS
 * holding a declaration we have no record of), then the TaxRIS call runs without a DB connection, then the outcome
 * is written back (see {@link TaxSubmissionCoordinator}).
 */
@Service
class DeclareTaxService implements DeclareTaxUseCase {

    private final TaxpayerTinPort taxpayerTinPort;
    private final TaxDeclarationRepository taxDeclarationRepository;
    private final TaxSubmissionCoordinator coordinator;

    DeclareTaxService(
            TaxpayerTinPort taxpayerTinPort,
            TaxDeclarationRepository taxDeclarationRepository,
            TaxSubmissionCoordinator coordinator) {
        this.taxpayerTinPort = taxpayerTinPort;
        this.taxDeclarationRepository = taxDeclarationRepository;
        this.coordinator = coordinator;
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

        if (taxDeclarationRepository.existsActive(tin, declaration.getInvoiceNumber(), null)) {
            throw new DuplicateTaxInvoiceException(declaration.getInvoiceNumber());
        }
        try {
            taxDeclarationRepository.save(declaration);
        } catch (DataIntegrityViolationException e) {
            // Lost a race with a concurrent submission of the same invoice (unique index on tin + invoice number).
            throw new DuplicateTaxInvoiceException(declaration.getInvoiceNumber());
        }
        return coordinator.submitAndRecord(declaration).getId();
    }
}
