package com.example.ptin.auth.infrastructure.rest.request;

import jakarta.validation.constraints.NotBlank;

public record RequestOtpRequest(@NotBlank String mobileNumber) {
}
