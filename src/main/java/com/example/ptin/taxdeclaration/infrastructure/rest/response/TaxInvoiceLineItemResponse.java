package com.example.ptin.taxdeclaration.infrastructure.rest.response;

import com.example.ptin.taxdeclaration.domain.model.TaxInvoiceLineItem;

public record TaxInvoiceLineItemResponse(
        String hsCode,
        String hsName,
        String saleCount,
        String unitSale,
        String unitSaleAmount,
        String supplyAmount,
        String exciseAmount,
        String vatAmount,
        String saleAmount) {

    public static TaxInvoiceLineItemResponse from(TaxInvoiceLineItem item) {
        return new TaxInvoiceLineItemResponse(
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
}
