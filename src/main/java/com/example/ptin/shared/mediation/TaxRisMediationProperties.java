package com.example.ptin.shared.mediation;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Connection settings for the external TaxRIS mediation gateway, shared by every module that calls
 * one of its endpoints (PTIN issuance, tax declaration, ...) — one gateway, one set of credentials.
 */
@ConfigurationProperties(prefix = "ptin.mediation")
public record TaxRisMediationProperties(
        String baseUrl, String hashKey, String sys, long connectTimeoutMs, long readTimeoutMs) {
}
