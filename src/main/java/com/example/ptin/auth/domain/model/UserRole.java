package com.example.ptin.auth.domain.model;

public enum UserRole {
    APPLICANT,
    AUTHORIZER,
    /** Support staff; provisioned out-of-band like AUTHORIZER. */
    ADMIN
}
