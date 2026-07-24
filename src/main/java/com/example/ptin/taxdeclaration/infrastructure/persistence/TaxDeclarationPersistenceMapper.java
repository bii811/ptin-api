package com.example.ptin.taxdeclaration.infrastructure.persistence;

import com.example.ptin.shared.identity.UserId;
import com.example.ptin.taxdeclaration.domain.model.TaxDeclaration;
import com.example.ptin.taxdeclaration.domain.model.TaxDeclarationId;
import com.example.ptin.taxdeclaration.domain.model.TaxDeclarationStatus;
import com.example.ptin.taxdeclaration.domain.model.TaxInvoiceLineItem;
import java.util.List;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Component
class TaxDeclarationPersistenceMapper {

    private static final TypeReference<List<TaxDeclarationLineItemJson>> ITEMS_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;

    TaxDeclarationPersistenceMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    TaxDeclarationJpaEntity toEntity(TaxDeclaration declaration) {
        return new TaxDeclarationJpaEntity(
                declaration.getId().value(),
                declaration.getUserId().value(),
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
                writeItems(declaration.getItems()),
                declaration.getSubmittedAt(),
                declaration.getResultCode(),
                declaration.getResultMessage());
    }

    TaxDeclaration toDomain(TaxDeclarationJpaEntity entity) {
        List<TaxInvoiceLineItem> items =
                readItems(entity.getItemsJson()).stream().map(TaxDeclarationLineItemJson::toDomain).toList();

        return TaxDeclaration.reconstitute(
                new TaxDeclarationId(entity.getId()),
                new UserId(entity.getUserId()),
                TaxDeclarationStatus.valueOf(entity.getStatus()),
                entity.getTin(),
                entity.getInvoiceNumber(),
                entity.getInvoiceDate(),
                entity.getBuyerTin(),
                entity.getBuyerFullName(),
                entity.getSaleCount(),
                entity.getSupplyAmount(),
                entity.getServiceFee(),
                entity.getExciseAmount(),
                entity.getVatAmount(),
                entity.getSaleAmount(),
                entity.getDiscountAmount(),
                entity.getSaleCancelCount(),
                entity.getSaleCancelAmount(),
                items,
                entity.getSubmittedAt(),
                entity.getResultCode(),
                entity.getResultMessage());
    }

    private String writeItems(List<TaxInvoiceLineItem> items) {
        try {
            return objectMapper.writeValueAsString(items.stream().map(TaxDeclarationLineItemJson::from).toList());
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to serialize tax declaration line items", e);
        }
    }

    private List<TaxDeclarationLineItemJson> readItems(String itemsJson) {
        try {
            return objectMapper.readValue(itemsJson, ITEMS_TYPE);
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to deserialize tax declaration line items", e);
        }
    }
}
