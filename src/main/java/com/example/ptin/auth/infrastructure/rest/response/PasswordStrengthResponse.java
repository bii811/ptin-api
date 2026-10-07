package com.example.ptin.auth.infrastructure.rest.response;

import com.example.ptin.auth.domain.model.PasswordStrength;

/** {@code WEAK} means the password was accepted but lacks upper case, lower case and a digit. */
public record PasswordStrengthResponse(PasswordStrength passwordStrength) {
}
