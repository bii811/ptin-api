package com.example.ptin.auth.config;

import com.example.ptin.auth.domain.model.LockoutPolicy;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.password")
public record PasswordLoginProperties(int maxFailedAttempts, Duration lockDuration) {

    public LockoutPolicy lockoutPolicy() {
        return new LockoutPolicy(maxFailedAttempts, lockDuration);
    }
}
