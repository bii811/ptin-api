package com.example.ptin.taxdeclaration.domain.port.out;

import com.example.ptin.shared.identity.UserId;
import com.example.ptin.taxdeclaration.domain.model.TaxDeclaration;
import com.example.ptin.taxdeclaration.domain.model.TaxDeclarationId;
import java.util.List;
import java.util.Optional;

public interface TaxDeclarationRepository {

    Optional<TaxDeclaration> findById(TaxDeclarationId id);

    List<TaxDeclaration> findByUserId(UserId userId);

    /** Loads the row with a write lock (call inside a transaction) so concurrent retries serialise. */
    Optional<TaxDeclaration> findByIdForUpdate(TaxDeclarationId id);

    /** A declaration for this invoice that is not FAILED (pending, submitted or unknown), other than {@code excludeId}. */
    boolean existsActive(String tin, String invoiceNumber, TaxDeclarationId excludeId);

    TaxDeclaration save(TaxDeclaration declaration);
}
