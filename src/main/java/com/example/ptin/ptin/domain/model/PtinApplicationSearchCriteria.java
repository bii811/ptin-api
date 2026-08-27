package com.example.ptin.ptin.domain.model;

import java.time.Instant;

public record PtinApplicationSearchCriteria(
        PtinStatus status, String tin, String applicantName, Instant submittedFrom, Instant submittedTo) {
}
