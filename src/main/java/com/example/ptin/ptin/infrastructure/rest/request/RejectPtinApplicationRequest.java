package com.example.ptin.ptin.infrastructure.rest.request;

import jakarta.validation.constraints.NotBlank;

public record RejectPtinApplicationRequest(@NotBlank String reason) {
}
