package com.example.ptin.auth.domain.port.in;

/**
 * Password login. The username is the mobile number, or — once a TIN has been issued to the
 * account — the TIN. Exactly one of the two is set.
 */
public interface LoginWithPasswordUseCase {

    LoginResult login(LoginWithPasswordCommand command);

    record LoginWithPasswordCommand(String mobileNumber, String tin, String password) {
    }
}
