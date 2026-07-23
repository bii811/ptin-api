package com.example.ptin.auth.infrastructure.rest.request;

import jakarta.validation.constraints.NotBlank;

public record VerifyOtpRequest(@NotBlank String mobileNumber, @NotBlank String otpCode) {
}
