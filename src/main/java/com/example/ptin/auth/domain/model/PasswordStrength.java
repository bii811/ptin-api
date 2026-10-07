package com.example.ptin.auth.domain.model;

/** Advisory only: a {@code WEAK} password is still accepted, the client is just told. */
public enum PasswordStrength {
    WEAK,
    STRONG
}
