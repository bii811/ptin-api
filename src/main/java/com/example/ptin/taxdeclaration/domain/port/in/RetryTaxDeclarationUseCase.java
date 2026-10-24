package com.example.ptin.taxdeclaration.domain.port.in;

import com.example.ptin.shared.identity.UserId;
import com.example.ptin.taxdeclaration.domain.model.TaxDeclaration;
import com.example.ptin.taxdeclaration.domain.model.TaxDeclarationId;

public interface RetryTaxDeclarationUseCase {

    /** Re-submits a FAILED / UNKNOWN (or abandoned PENDING) declaration owned by {@code requester}. */
    TaxDeclaration retry(TaxDeclarationId id, UserId requester);
}
