package com.example.ptin.auth.domain.port.in;

import com.example.ptin.shared.identity.UserId;

/** Support fallback for a user who cannot receive an OTP. Admin-only. */
public interface AssignTemporaryPasswordUseCase {

    /** @return the plaintext temporary password, shown once for the admin to relay to the user */
    String assign(AssignTemporaryPasswordCommand command);

    record AssignTemporaryPasswordCommand(UserId actorId, String mobileNumber) {
    }
}
