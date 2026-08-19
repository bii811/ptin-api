package com.example.ptin.shared.security.token;

import java.time.Instant;

/**
 * A freshly minted access token together with the expiry that was actually baked into it. Callers
 * must never recompute the expiry themselves — deriving it from a second {@code Instant.now()} lets
 * the advertised expiry drift away from the token's own {@code exp} claim.
 */
public record IssuedToken(String value, Instant issuedAt, Instant expiresAt) {
}
