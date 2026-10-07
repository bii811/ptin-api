package com.example.ptin.auth.infrastructure.rest.response;

import com.example.ptin.auth.domain.port.in.LoginResult;
import java.time.Instant;

/**
 * Either a session ({@code accessToken}...) or, when {@code passwordChangeRequired}, only a
 * {@code resetToken} to submit to {@code /forgot-password/reset}.
 */
public record AuthTokenResponse(
        String accessToken,
        String tokenType,
        Instant expiresAt,
        String role,
        boolean passwordChangeRequired,
        String resetToken,
        Instant resetTokenExpiresAt) {

    public static AuthTokenResponse from(LoginResult result) {
        if (result.passwordResetToken() != null) {
            var reset = result.passwordResetToken();
            return new AuthTokenResponse(null, null, null, null, true, reset.token(), reset.expiresAt());
        }
        var token = result.accessToken();
        return new AuthTokenResponse(
                token.accessToken(), "Bearer", token.expiresAt(), token.role(), false, null, null);
    }
}
