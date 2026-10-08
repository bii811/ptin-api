package com.example.ptin.auth.domain.port.in;

/**
 * Password login. Applicants identify by mobile number, or — once a TIN has been issued — the TIN;
 * staff (ADMIN/SUPERADMIN) by username. Exactly one of the three is set.
 */
public interface LoginWithPasswordUseCase {

    LoginResult login(LoginWithPasswordCommand command);

    record LoginWithPasswordCommand(String mobileNumber, String tin, String username, String password) {
    }
}
