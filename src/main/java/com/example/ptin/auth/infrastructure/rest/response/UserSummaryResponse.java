package com.example.ptin.auth.infrastructure.rest.response;

/**
 * @param tin         null until a TIN has been issued
 * @param passwordSet whether TIN + password login is available for this account
 */
public record UserSummaryResponse(
        String mobileNumber, String role, boolean profileComplete, String tin, boolean passwordSet) {
}
