package com.example.ptin.auth.domain.port.in;

import com.example.ptin.shared.identity.UserId;

/**
 * Sets the initial password, or replaces an existing one. {@code currentPassword} is required only
 * when a password is already set — on first use the caller has already proven identity via OTP login.
 */
public interface SetPasswordUseCase {

    void setPassword(SetPasswordCommand command);

    record SetPasswordCommand(UserId userId, String currentPassword, String newPassword) {
    }
}
