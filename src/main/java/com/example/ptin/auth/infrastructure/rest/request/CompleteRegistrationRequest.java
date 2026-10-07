package com.example.ptin.auth.infrastructure.rest.request;

import jakarta.validation.constraints.NotBlank;

public record CompleteRegistrationRequest(@NotBlank String registrationToken, @NotBlank String password) {
}
