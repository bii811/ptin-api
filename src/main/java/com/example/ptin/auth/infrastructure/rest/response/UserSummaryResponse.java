package com.example.ptin.auth.infrastructure.rest.response;

public record UserSummaryResponse(String mobileNumber, String role, boolean profileComplete) {
}
