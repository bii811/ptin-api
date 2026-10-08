package com.example.ptin.shared.mediation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Audit row for one outbound TaxRIS request. Append-only: no setters. */
@Entity
@Table(name = "taxris_api_call_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TaxRisApiCallLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "function_source", nullable = false, length = 100)
    private String functionSource;

    @Column(name = "reference_id", length = 64)
    private String referenceId;

    @Column(name = "triggered_by", length = 64)
    private String triggeredBy;

    @Column(name = "endpoint_url", nullable = false, length = 500)
    private String endpointUrl;

    @Column(name = "request_headers")
    private String requestHeaders;

    @Column(name = "request_body")
    private String requestBody;

    @Column(name = "response_body")
    private String responseBody;

    @Column(name = "result_code", length = 10)
    private String resultCode;

    @Column(name = "result_message", length = 1000)
    private String resultMessage;

    @Column(name = "attempt_no", nullable = false)
    private int attemptNo;

    @Column(name = "called_at", nullable = false)
    private Instant calledAt;

    TaxRisApiCallLog(CallContext ctx, String endpointUrl, String requestHeaders, String requestBody,
            String responseBody, String resultCode, String resultMessage, Instant calledAt) {
        this.functionSource = ctx.functionSource();
        this.referenceId = ctx.referenceId();
        this.triggeredBy = ctx.triggeredBy();
        this.endpointUrl = endpointUrl;
        this.requestHeaders = requestHeaders;
        this.requestBody = requestBody;
        this.responseBody = responseBody;
        this.resultCode = resultCode;
        this.resultMessage = resultMessage == null || resultMessage.length() <= 1000
                ? resultMessage : resultMessage.substring(0, 1000);
        this.attemptNo = ctx.attemptNo();
        this.calledAt = calledAt;
    }
}
