package com.example.ptin.shared.time;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Exposes the wall clock as an injectable bean so time-sensitive domain rules (OTP expiry, lockout
 * windows, token expiry) can be driven deterministically from tests instead of calling
 * {@code Instant.now()} at half a dozen unrelated call sites.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
