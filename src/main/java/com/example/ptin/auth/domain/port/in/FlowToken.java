package com.example.ptin.auth.domain.port.in;

import java.time.Instant;

/** A short-lived, signed proof that one step of a multi-step flow (registration, password reset) was passed. */
public record FlowToken(String token, Instant expiresAt) {
}
