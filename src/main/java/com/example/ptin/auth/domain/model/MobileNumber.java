package com.example.ptin.auth.domain.model;

import java.util.regex.Pattern;

public record MobileNumber(String value) {

    // Laos mobile numbers: leading "20" + 8 digits. Adjust if the actual carrier format differs.
    private static final Pattern FORMAT = Pattern.compile("^20\\d{8}$");

    public MobileNumber {
        if (value == null || !FORMAT.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid mobile number format: " + value);
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
