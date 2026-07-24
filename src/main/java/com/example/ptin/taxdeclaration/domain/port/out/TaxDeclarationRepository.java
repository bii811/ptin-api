package com.example.ptin.taxdeclaration.domain.port.out;

import com.example.ptin.shared.identity.UserId;
import com.example.ptin.taxdeclaration.domain.model.TaxDeclaration;
import com.example.ptin.taxdeclaration.domain.model.TaxDeclarationId;
import java.util.List;
import java.util.Optional;

public interface TaxDeclarationRepository {

    Optional<TaxDeclaration> findById(TaxDeclarationId id);

    List<TaxDeclaration> findByUserId(UserId userId);

    TaxDeclaration save(TaxDeclaration declaration);
}
