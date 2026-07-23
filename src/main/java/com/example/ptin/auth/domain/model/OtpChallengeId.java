package com.example.ptin.auth.domain.model;

import java.util.Objects;
import java.util.UUID;

public record OtpChallengeId(UUID value) {

    public OtpChallengeId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static OtpChallengeId generate() {
        return new OtpChallengeId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
