package com.example.ptin.taxdeclaration.domain.model;

public enum TaxDeclarationStatus {
    PENDING,
    SUBMITTED,
    /** TaxRIS explicitly rejected it. */
    FAILED,
    /** No usable reply (timeout / transport error / unparseable): TaxRIS may or may not have recorded it. */
    UNKNOWN
}
