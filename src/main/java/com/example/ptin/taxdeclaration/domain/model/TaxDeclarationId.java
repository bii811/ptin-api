package com.example.ptin.taxdeclaration.domain.model;

import java.util.Objects;
import java.util.UUID;

public record TaxDeclarationId(UUID value) {

    public TaxDeclarationId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static TaxDeclarationId generate() {
        return new TaxDeclarationId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
