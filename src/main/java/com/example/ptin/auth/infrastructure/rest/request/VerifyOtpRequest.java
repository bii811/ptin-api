package com.example.ptin.auth.infrastructure.rest.request;

import com.example.ptin.auth.domain.model.MobileNumber;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyOtpRequest(
        @NotBlank @Pattern(regexp = MobileNumber.PATTERN, message = "must be a valid mobile number") String mobileNumber,
        @NotBlank String otpCode) {
}
