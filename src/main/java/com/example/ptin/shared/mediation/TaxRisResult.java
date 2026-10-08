package com.example.ptin.shared.mediation;

/** {@code success} means HTTP 2xx and {@code Result.CD == "000"}. {@code code} is null if no parseable response. */
public record TaxRisResult<T>(boolean success, String code, String message, T data) {

    static <T> TaxRisResult<T> failure(String code, String message) {
        return new TaxRisResult<>(false, code, message, null);
    }
}
