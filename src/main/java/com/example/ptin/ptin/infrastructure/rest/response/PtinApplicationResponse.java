package com.example.ptin.ptin.infrastructure.rest.response;

import com.example.ptin.ptin.domain.model.PtinApplication;
import java.time.Instant;

public record PtinApplicationResponse(
        String id,
        String userId,
        String status,
        Instant submittedAt,
        Instant approvedAt,
        Instant rejectedAt,
        String rejectionReason,
        Instant issuedAt,
        String failureReason,
        int retryCount,
        String tin,
        String givenName,
        String familyName) {

    public static PtinApplicationResponse from(PtinApplication application) {
        return new PtinApplicationResponse(
                application.getId().toString(),
                application.getUserId().toString(),
                application.getStatus().name(),
                application.getSubmittedAt(),
                application.getApprovedAt(),
                application.getRejectedAt(),
                application.getRejectionReason(),
                application.getIssuedAt(),
                application.getFailureReason(),
                application.getRetryCount(),
                application.getTin() == null ? null : application.getTin().value(),
                application.getPersonalInfo().givenName(),
                application.getPersonalInfo().familyName());
    }
}
