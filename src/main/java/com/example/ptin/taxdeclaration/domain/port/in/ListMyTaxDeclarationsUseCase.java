package com.example.ptin.taxdeclaration.domain.port.in;

import com.example.ptin.shared.identity.UserId;
import com.example.ptin.taxdeclaration.domain.model.TaxDeclaration;
import java.util.List;

public interface ListMyTaxDeclarationsUseCase {

    List<TaxDeclaration> listMine(UserId userId);
}
