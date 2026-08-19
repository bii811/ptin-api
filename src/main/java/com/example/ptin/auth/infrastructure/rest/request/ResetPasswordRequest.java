package com.example.ptin.auth.infrastructure.rest.request;

import com.example.ptin.auth.domain.model.MobileNumber;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank @Size(max = 12) String tin,
        @NotBlank @Pattern(regexp = MobileNumber.PATTERN, message = "must be a valid mobile number") String mobileNumber,
        @NotBlank String otpCode,
        @NotBlank String newPassword) {
}
