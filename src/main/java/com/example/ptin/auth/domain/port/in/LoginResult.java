package com.example.ptin.auth.domain.port.in;

/**
 * Exactly one is set: a session, or — when the account still has a support-assigned temporary
 * password — a password-reset token that must be spent on {@code /forgot-password/reset} first.
 */
public record LoginResult(AuthToken accessToken, FlowToken passwordResetToken) {

    public static LoginResult authenticated(AuthToken token) {
        return new LoginResult(token, null);
    }

    public static LoginResult passwordChangeRequired(FlowToken resetToken) {
        return new LoginResult(null, resetToken);
    }
}
