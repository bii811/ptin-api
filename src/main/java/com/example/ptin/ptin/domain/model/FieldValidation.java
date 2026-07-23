package com.example.ptin.ptin.domain.model;

final class FieldValidation {

    private FieldValidation() {
    }

    static void requireMaxLength(String fieldName, String value, int maxLength) {
        if (value != null && value.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " exceeds max length of " + maxLength);
        }
    }

    static void requireExactLength(String fieldName, String value, int length) {
        if (value == null || value.length() != length) {
            throw new IllegalArgumentException(fieldName + " must be exactly " + length + " characters");
        }
    }

    static void requireYesNo(String fieldName, String value) {
        if (value != null && !value.equals("Y") && !value.equals("N")) {
            throw new IllegalArgumentException(fieldName + " must be 'Y' or 'N'");
        }
    }
}
