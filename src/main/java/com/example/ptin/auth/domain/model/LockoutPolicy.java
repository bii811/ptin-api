package com.example.ptin.auth.domain.model;

import java.time.Duration;

/**
 * How many consecutive wrong passwords a user gets before the account is temporarily locked, and for
 * how long. A temporary lock is used rather than a permanent one so an attacker cannot lock a
 * legitimate taxpayer out of filing by spraying their TIN.
 */
public record LockoutPolicy(int maxFailedAttempts, Duration lockDuration) {

    public LockoutPolicy {
        if (maxFailedAttempts < 1) {
            throw new IllegalArgumentException("maxFailedAttempts must be at least 1");
        }
        if (lockDuration == null || lockDuration.isNegative() || lockDuration.isZero()) {
            throw new IllegalArgumentException("lockDuration must be positive");
        }
    }
}
