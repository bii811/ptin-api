package com.example.ptin.taxdeclaration.application.usecase;

import com.example.ptin.shared.identity.UserId;
import com.example.ptin.taxdeclaration.domain.exception.DuplicateTaxInvoiceException;
import com.example.ptin.taxdeclaration.domain.exception.TaxDeclarationNotFoundException;
import com.example.ptin.taxdeclaration.domain.model.TaxDeclaration;
import com.example.ptin.taxdeclaration.domain.model.TaxDeclarationId;
import com.example.ptin.taxdeclaration.domain.port.out.TaxDeclarationRepository;
import java.time.Clock;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Its own bean so {@code @Transactional} goes through the proxy. The row is locked {@code FOR UPDATE} and flipped
 * to PENDING in one short transaction, so two concurrent retries cannot both reach TaxRIS: the second one waits,
 * then finds PENDING and is refused (409).
 */
@Component
class TaxDeclarationTransactionalGateway {

    private final TaxDeclarationRepository taxDeclarationRepository;
    private final Clock clock;

    TaxDeclarationTransactionalGateway(TaxDeclarationRepository taxDeclarationRepository, Clock clock) {
        this.taxDeclarationRepository = taxDeclarationRepository;
        this.clock = clock;
    }

    @Transactional
    TaxDeclaration claimForRetry(TaxDeclarationId id, UserId requester) {
        TaxDeclaration declaration =
                taxDeclarationRepository.findByIdForUpdate(id).orElseThrow(() -> new TaxDeclarationNotFoundException(id));
        if (!declaration.getUserId().equals(requester)) {
            throw new AccessDeniedException("Not permitted to retry this tax declaration");
        }
        declaration.startRetry(clock.instant());
        // A newer declaration for the same invoice may have been filed after this one failed.
        if (taxDeclarationRepository.existsActive(declaration.getTin(), declaration.getInvoiceNumber(), id)) {
            throw new DuplicateTaxInvoiceException(declaration.getInvoiceNumber());
        }
        return taxDeclarationRepository.save(declaration);
    }
}
