package com.example.ptin.auth.domain.port.in;

import com.example.ptin.auth.domain.model.PasswordStrength;

/** Registration step C: registration token + password creates the active account. */
public interface CompleteRegistrationUseCase {

    PasswordStrength complete(CompleteRegistrationCommand command);

    record CompleteRegistrationCommand(String registrationToken, String password) {
    }
}
