package com.example.ptin.shared.integration.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "external_api_call_logs")
public class ExternalApiCallLogJpaEntity implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column(name = "system_name", nullable = false, length = 100)
    private String systemName;

    @Column(name = "endpoint", nullable = false, length = 500)
    private String endpoint;

    @Column(name = "http_method", nullable = false, length = 10)
    private String httpMethod;

    @Column(name = "request_body")
    private String requestBody;

    @Column(name = "response_body")
    private String responseBody;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "success", nullable = false)
    private boolean success;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "duration_ms", nullable = false)
    private long durationMs;

    @Column(name = "called_at", nullable = false)
    private Instant calledAt;

    protected ExternalApiCallLogJpaEntity() {
    }

    public ExternalApiCallLogJpaEntity(UUID id, String systemName, String endpoint, String httpMethod,
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

    @Override
    public UUID getId() {
        return id;
    }

    /**
     * These log rows are write-once and never reloaded via JPA before saving, so there is no
     * "existing row" case to detect - every save is an insert.
     */
    @Override
    public boolean isNew() {
        return true;
    }
}
