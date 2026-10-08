package com.example.ptin.shared.mediation;

/**
 * Who/why of a TaxRIS call, written to the audit log. {@code triggeredBy} is a user id or
 * {@code "SYSTEM"}; {@code referenceId} is the business record the call is about.
 */
public record CallContext(String functionSource, String referenceId, String triggeredBy, int attemptNo) {
}
