package com.example.ptin.auth.infrastructure.rest.response;

import java.time.Instant;

public record AuthTokenResponse(String accessToken, String tokenType, Instant expiresAt, String role) {

    public static AuthTokenResponse of(String accessToken, Instant expiresAt, String role) {
        return new AuthTokenResponse(accessToken, "Bearer", expiresAt, role);
    }
}
