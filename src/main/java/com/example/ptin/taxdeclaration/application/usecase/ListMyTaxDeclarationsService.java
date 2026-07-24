package com.example.ptin.taxdeclaration.application.usecase;

import com.example.ptin.shared.identity.UserId;
import com.example.ptin.taxdeclaration.domain.model.TaxDeclaration;
import com.example.ptin.taxdeclaration.domain.port.in.ListMyTaxDeclarationsUseCase;
import com.example.ptin.taxdeclaration.domain.port.out.TaxDeclarationRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class ListMyTaxDeclarationsService implements ListMyTaxDeclarationsUseCase {

    private final TaxDeclarationRepository taxDeclarationRepository;

    ListMyTaxDeclarationsService(TaxDeclarationRepository taxDeclarationRepository) {
        this.taxDeclarationRepository = taxDeclarationRepository;
    }

    @Override
    public List<TaxDeclaration> listMine(UserId userId) {
        return taxDeclarationRepository.findByUserId(userId);
    }
}
