package com.example.ptin.ptin.domain.model;

import java.util.Objects;
import java.util.UUID;

public record PtinApplicationId(UUID value) {

    public PtinApplicationId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static PtinApplicationId generate() {
        return new PtinApplicationId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
