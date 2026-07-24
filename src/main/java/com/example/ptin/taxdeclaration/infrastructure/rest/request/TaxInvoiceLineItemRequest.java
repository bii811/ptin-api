package com.example.ptin.taxdeclaration.infrastructure.rest.request;

import com.example.ptin.taxdeclaration.domain.model.TaxInvoiceLineItem;
import jakarta.validation.constraints.NotBlank;

public record TaxInvoiceLineItemRequest(
        @NotBlank String hsCode,
        @NotBlank String hsName,
        @NotBlank String saleCount,
        String unitSale,
        String unitSaleAmount,
        @NotBlank String supplyAmount,
        String exciseAmount,
        String vatAmount,
        @NotBlank String saleAmount) {

    public TaxInvoiceLineItem toDomain() {
        return new TaxInvoiceLineItem(
                hsCode, hsName, saleCount, unitSale, unitSaleAmount, supplyAmount, exciseAmount, vatAmount, saleAmount);
    }
}
