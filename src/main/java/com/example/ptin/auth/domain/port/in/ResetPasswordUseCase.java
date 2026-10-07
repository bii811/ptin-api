package com.example.ptin.auth.domain.port.in;

import com.example.ptin.auth.domain.model.PasswordStrength;

/** Forgotten-password step C: password-reset token + new password. */
public interface ResetPasswordUseCase {

    PasswordStrength resetPassword(ResetPasswordCommand command);

    record ResetPasswordCommand(String resetToken, String newPassword) {
    }
}
