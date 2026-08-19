package com.example.ptin.auth.infrastructure.rest.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginWithPasswordRequest(@NotBlank @Size(max = 12) String tin, @NotBlank String password) {
}
