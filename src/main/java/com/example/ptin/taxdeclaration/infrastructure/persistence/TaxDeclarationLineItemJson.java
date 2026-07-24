package com.example.ptin.taxdeclaration.infrastructure.persistence;

import com.example.ptin.taxdeclaration.domain.model.TaxInvoiceLineItem;

/**
 * Plain persistence-side mirror of {@link TaxInvoiceLineItem}, serialized as the {@code items_json}
 * column. Kept separate so the domain model stays free of Jackson - this shape is an internal
 * storage format we control, not an external API contract.
 */
record TaxDeclarationLineItemJson(
        String hsCode,
        String hsName,
        String saleCount,
        String unitSale,
        String unitSaleAmount,
        String supplyAmount,
        String exciseAmount,
        String vatAmount,
        String saleAmount) {

    static TaxDeclarationLineItemJson from(TaxInvoiceLineItem item) {
        return new TaxDeclarationLineItemJson(
                item.hsCode(),
                item.hsName(),
                item.saleCount(),
                item.unitSale(),
                item.unitSaleAmount(),
                item.supplyAmount(),
                item.exciseAmount(),
                item.vatAmount(),
                item.saleAmount());
    }

    TaxInvoiceLineItem toDomain() {
        return new TaxInvoiceLineItem(
                hsCode, hsName, saleCount, unitSale, unitSaleAmount, supplyAmount, exciseAmount, vatAmount, saleAmount);
    }
}
