package com.example.ptin.taxdeclaration.domain.port.in;

import com.example.ptin.shared.identity.UserId;
import com.example.ptin.taxdeclaration.domain.model.TaxDeclaration;
import com.example.ptin.taxdeclaration.domain.model.TaxDeclarationId;

public interface GetTaxDeclarationUseCase {

    TaxDeclaration getById(TaxDeclarationId id, UserId requestingUserId);
}
