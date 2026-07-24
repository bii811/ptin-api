package com.example.ptin.taxdeclaration.infrastructure.rest.request;

import com.example.ptin.shared.identity.UserId;
import com.example.ptin.taxdeclaration.domain.port.in.DeclareTaxUseCase.DeclareTaxCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record DeclareTaxRequest(
        @NotBlank String invoiceNumber,
        @NotBlank String invoiceDate,
        String buyerTin,
        String buyerFullName,
        @NotBlank String saleCount,
        @NotBlank String supplyAmount,
        String serviceFee,
        String exciseAmount,
        String vatAmount,
        @NotBlank String saleAmount,
        String discountAmount,
        String saleCancelCount,
        String saleCancelAmount,
        @NotEmpty @Valid List<TaxInvoiceLineItemRequest> items) {

    public DeclareTaxCommand toCommand(UserId userId) {
        return new DeclareTaxCommand(
                userId,
                invoiceNumber,
                invoiceDate,
                buyerTin,
                buyerFullName,
                saleCount,
                supplyAmount,
                serviceFee,
                exciseAmount,
                vatAmount,
                saleAmount,
                discountAmount,
                saleCancelCount,
                saleCancelAmount,
                items.stream().map(TaxInvoiceLineItemRequest::toDomain).toList());
    }
}
