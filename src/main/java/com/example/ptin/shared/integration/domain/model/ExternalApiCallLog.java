package com.example.ptin.shared.integration.domain.model;

import java.time.Instant;
import java.util.UUID;

public class ExternalApiCallLog {

    private final UUID id;
    private final String systemName;
    private final String endpoint;
    private final String httpMethod;
    private final String requestBody;
    private final String responseBody;
    private final Integer statusCode;
    private final boolean success;
    private final String errorMessage;
    private final long durationMs;
    private final Instant calledAt;

    private ExternalApiCallLog(UUID id, String systemName, String endpoint, String httpMethod,
            String requestBody, String responseBody, Integer statusCode, boolean success,
            String errorMessage, long durationMs, Instant calledAt) {
        this.id = id;
        this.systemName = systemName;
        this.endpoint = endpoint;
        this.httpMethod = httpMethod;
        this.requestBody = requestBody;
        this.responseBody = responseBody;
        this.statusCode = statusCode;
        this.success = success;
        this.errorMessage = errorMessage;
        this.durationMs = durationMs;
        this.calledAt = calledAt;
    }

    public static ExternalApiCallLog of(String systemName, String endpoint, String httpMethod,
            String requestBody, String responseBody, Integer statusCode, boolean success,
            String errorMessage, long durationMs, Instant calledAt) {
        return new ExternalApiCallLog(UUID.randomUUID(), systemName, endpoint, httpMethod, requestBody,
                responseBody, statusCode, success, errorMessage, durationMs, calledAt);
    }

    public UUID getId() {
        return id;
    }

    public String getSystemName() {
        return systemName;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public Instant getCalledAt() {
        return calledAt;
    }
}
