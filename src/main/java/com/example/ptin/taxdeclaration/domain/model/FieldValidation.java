package com.example.ptin.taxdeclaration.domain.model;

final class FieldValidation {

    private FieldValidation() {
    }

    static void requireNotBlank(String fieldName, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }

    static void requireMaxLength(String fieldName, String value, int maxLength) {
        if (value != null && value.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " exceeds max length of " + maxLength);
        }
    }
}
