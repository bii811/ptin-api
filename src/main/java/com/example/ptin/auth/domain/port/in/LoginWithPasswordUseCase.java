package com.example.ptin.auth.domain.port.in;

/** TIN + password login, available once the user has an issued TIN and has set a password. */
public interface LoginWithPasswordUseCase {

    AuthToken login(LoginWithPasswordCommand command);

    record LoginWithPasswordCommand(String tin, String password) {
    }
}
