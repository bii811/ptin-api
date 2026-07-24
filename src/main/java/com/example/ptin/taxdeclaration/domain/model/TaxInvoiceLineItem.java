package com.example.ptin.taxdeclaration.domain.model;

public record TaxInvoiceLineItem(
        String hsCode,
        String hsName,
        String saleCount,
        String unitSale,
        String unitSaleAmount,
        String supplyAmount,
        String exciseAmount,
        String vatAmount,
        String saleAmount) {

    public TaxInvoiceLineItem {
        FieldValidation.requireNotBlank("hsCode", hsCode);
        FieldValidation.requireMaxLength("hsCode", hsCode, 32);
        FieldValidation.requireNotBlank("hsName", hsName);
        FieldValidation.requireMaxLength("hsName", hsName, 300);
        FieldValidation.requireNotBlank("saleCount", saleCount);
        FieldValidation.requireMaxLength("saleCount", saleCount, 10);
        FieldValidation.requireMaxLength("unitSale", unitSale, 50);
        FieldValidation.requireMaxLength("unitSaleAmount", unitSaleAmount, 28);
        FieldValidation.requireNotBlank("supplyAmount", supplyAmount);
        FieldValidation.requireMaxLength("supplyAmount", supplyAmount, 28);
        FieldValidation.requireMaxLength("exciseAmount", exciseAmount, 28);
        FieldValidation.requireMaxLength("vatAmount", vatAmount, 28);
        FieldValidation.requireNotBlank("saleAmount", saleAmount);
        FieldValidation.requireMaxLength("saleAmount", saleAmount, 28);
    }
}
