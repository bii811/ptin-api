package com.example.ptin.taxdeclaration.infrastructure.mediation;

import com.example.ptin.taxdeclaration.domain.model.TaxInvoiceLineItem;
import com.fasterxml.jackson.annotation.JsonProperty;

record CrsPfmLineItemPayload(
        @JsonProperty("HS_CD") String hsCode,
        @JsonProperty("HS_NM") String hsName,
        @JsonProperty("SALE_CNT") String saleCount,
        @JsonProperty("UNIT_SALE") String unitSale,
        @JsonProperty("UNIT_SALE_AMT") String unitSaleAmount,
        @JsonProperty("SUPL_AMT") String supplyAmount,
        @JsonProperty("EXCISE_AMT") String exciseAmount,
        @JsonProperty("VAT_AMT") String vatAmount,
        @JsonProperty("SALE_AMT") String saleAmount) {

    static CrsPfmLineItemPayload from(TaxInvoiceLineItem item) {
        return new CrsPfmLineItemPayload(
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
