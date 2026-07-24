package com.example.ptin.taxdeclaration.infrastructure.mediation;

import com.example.ptin.taxdeclaration.domain.model.TaxDeclaration;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

record CrsPfmsPayload(
        @JsonProperty("TIN") String tin,
        @JsonProperty("INV_NO") String invoiceNumber,
        @JsonProperty("INV_DD") String invoiceDate,
        @JsonProperty("BY_TIN") String buyerTin,
        @JsonProperty("BY_FULL_NM") String buyerFullName,
        @JsonProperty("SALE_CNT") String saleCount,
        @JsonProperty("SUPL_AMT") String supplyAmount,
        @JsonProperty("SVC_FEE") String serviceFee,
        @JsonProperty("EXCISE_AMT") String exciseAmount,
        @JsonProperty("VAT_AMT") String vatAmount,
        @JsonProperty("SALE_AMT") String saleAmount,
        @JsonProperty("DISC_AMT") String discountAmount,
        @JsonProperty("SALE_CNCL_CNT") String saleCancelCount,
        @JsonProperty("SALE_CNCL_AMT") String saleCancelAmount,
        @JsonProperty("list") List<CrsPfmLineItemPayload> list) {

    static CrsPfmsPayload from(TaxDeclaration declaration) {
        return new CrsPfmsPayload(
                declaration.getTin(),
                declaration.getInvoiceNumber(),
                declaration.getInvoiceDate(),
                declaration.getBuyerTin(),
                declaration.getBuyerFullName(),
                declaration.getSaleCount(),
                declaration.getSupplyAmount(),
                declaration.getServiceFee(),
                declaration.getExciseAmount(),
                declaration.getVatAmount(),
                declaration.getSaleAmount(),
                declaration.getDiscountAmount(),
                declaration.getSaleCancelCount(),
                declaration.getSaleCancelAmount(),
                declaration.getItems().stream().map(CrsPfmLineItemPayload::from).toList());
    }
}
