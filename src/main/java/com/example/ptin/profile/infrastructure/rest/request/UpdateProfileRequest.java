package com.example.ptin.profile.infrastructure.rest.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequest(@NotBlank String firstName, @NotBlank String lastName, String avatarUrl) {
}
