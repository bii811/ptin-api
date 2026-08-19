package com.example.ptin.auth.domain.port.in;

import java.time.Instant;

/** An issued access token. {@code expiresAt} always mirrors the token's own {@code exp} claim. */
public record AuthToken(String accessToken, Instant expiresAt, String role) {
}
