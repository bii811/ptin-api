package com.example.ptin.auth.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OTP issuance and verification policy.
 *
 * @param codeLength              digits in a generated code
 * @param ttl                     how long an issued code stays usable
 * @param maxAttempts             wrong guesses allowed against one code before it is burned
 * @param resendCooldown          minimum gap between two OTP requests for the same number/purpose
 * @param maxRequestsPerWindow    OTP requests allowed per number/purpose within {@code requestWindow}
 * @param requestWindow           the rate-limiting window
 * @param exposeInResponse        echo the plaintext code in the API response (no SMS gateway wired up)
 */
@ConfigurationProperties(prefix = "otp")
public record OtpProperties(
        int codeLength,
        Duration ttl,
        int maxAttempts,
        Duration resendCooldown,
        int maxRequestsPerWindow,
        Duration requestWindow,
        boolean exposeInResponse) {
}
