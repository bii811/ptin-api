package com.example.ptin.taxdeclaration.infrastructure.rest.response;

import com.example.ptin.taxdeclaration.domain.model.TaxDeclaration;
import java.time.Instant;
import java.util.List;

public record TaxDeclarationResponse(
        String id,
        String userId,
        String status,
        String tin,
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
        List<TaxInvoiceLineItemResponse> items,
        Instant submittedAt,
        String resultCode,
        String resultMessage) {

    public static TaxDeclarationResponse from(TaxDeclaration declaration) {
        return new TaxDeclarationResponse(
                declaration.getId().toString(),
                declaration.getUserId().toString(),
                declaration.getStatus().name(),
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
                declaration.getItems().stream().map(TaxInvoiceLineItemResponse::from).toList(),
                declaration.getSubmittedAt(),
                declaration.getResultCode(),
                declaration.getResultMessage());
    }
}
