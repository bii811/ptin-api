package com.example.ptin.auth.domain.model;

import java.util.regex.Pattern;

public record MobileNumber(String value) {

    /** Laos mobile numbers: leading "20" + 8 digits. Adjust if the actual carrier format differs. */
    public static final String PATTERN = "^20\\d{8}$";

    private static final Pattern FORMAT = Pattern.compile(PATTERN);

    public MobileNumber {
        if (value == null || !FORMAT.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid mobile number format");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
