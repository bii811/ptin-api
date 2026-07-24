package com.example.ptin.taxdeclaration.domain.port.in;

import com.example.ptin.shared.identity.UserId;
import com.example.ptin.taxdeclaration.domain.model.TaxDeclarationId;
import com.example.ptin.taxdeclaration.domain.model.TaxInvoiceLineItem;
import java.util.List;

public interface DeclareTaxUseCase {

    TaxDeclarationId declare(DeclareTaxCommand command);

    record DeclareTaxCommand(
            UserId userId,
            String invoiceNumber,
            String invoiceDate,
            String buyerTin,
            String buyerFullName,
            String saleCount,
            String supplyAmount,
            String serviceFee,
            String exciseAmount,
            String vatAmount,
            String saleAmount,
            String discountAmount,
            String saleCancelCount,
            String saleCancelAmount,
            List<TaxInvoiceLineItem> items) {
    }
}
