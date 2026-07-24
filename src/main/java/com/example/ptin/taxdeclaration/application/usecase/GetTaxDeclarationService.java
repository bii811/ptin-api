package com.example.ptin.taxdeclaration.application.usecase;

import com.example.ptin.shared.identity.UserId;
import com.example.ptin.taxdeclaration.domain.exception.TaxDeclarationNotFoundException;
import com.example.ptin.taxdeclaration.domain.model.TaxDeclaration;
import com.example.ptin.taxdeclaration.domain.model.TaxDeclarationId;
import com.example.ptin.taxdeclaration.domain.port.in.GetTaxDeclarationUseCase;
import com.example.ptin.taxdeclaration.domain.port.out.TaxDeclarationRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class GetTaxDeclarationService implements GetTaxDeclarationUseCase {

    private final TaxDeclarationRepository taxDeclarationRepository;

    GetTaxDeclarationService(TaxDeclarationRepository taxDeclarationRepository) {
        this.taxDeclarationRepository = taxDeclarationRepository;
    }

    @Override
    public TaxDeclaration getById(TaxDeclarationId id, UserId requestingUserId) {
        TaxDeclaration declaration =
                taxDeclarationRepository.findById(id).orElseThrow(() -> new TaxDeclarationNotFoundException(id));
        if (!declaration.getUserId().equals(requestingUserId)) {
            throw new AccessDeniedException("Not permitted to view this tax declaration");
        }
        return declaration;
    }
}
