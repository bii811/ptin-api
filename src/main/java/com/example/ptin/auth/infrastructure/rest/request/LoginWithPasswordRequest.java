package com.example.ptin.auth.infrastructure.rest.request;

import com.example.ptin.auth.domain.model.MobileNumber;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Exactly one of {@code mobileNumber} / {@code tin} / {@code username} identifies the account. */
public record LoginWithPasswordRequest(
        @Pattern(regexp = MobileNumber.PATTERN, message = "must be a valid mobile number") String mobileNumber,
        @Size(max = 12) String tin,
        @Size(max = 50) String username,
        @NotBlank String password) {
}
