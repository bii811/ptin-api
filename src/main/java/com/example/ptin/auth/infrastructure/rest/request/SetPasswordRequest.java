package com.example.ptin.auth.infrastructure.rest.request;

import jakarta.validation.constraints.NotBlank;

/** {@code currentPassword} is required only when the account already has a password. */
public record SetPasswordRequest(String currentPassword, @NotBlank String newPassword) {
}
