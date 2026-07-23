package com.example.ptin.ptin.infrastructure.mediation;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ptin.mediation")
public record TinMediationProperties(
        String baseUrl, String hashKey, String sys, long connectTimeoutMs, long readTimeoutMs) {
}
