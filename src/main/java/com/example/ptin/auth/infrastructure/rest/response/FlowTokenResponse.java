package com.example.ptin.auth.infrastructure.rest.response;

import java.time.Instant;

public record FlowTokenResponse(String token, Instant expiresAt) {
}
