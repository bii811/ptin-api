package com.example.ptin.auth.domain.port.in;

/** Forgotten-password step A. Answers identically whether or not the account exists. */
public interface RequestPasswordResetOtpUseCase {

    OtpIssued requestOtp(RequestPasswordResetOtpCommand command);

    record RequestPasswordResetOtpCommand(String mobileNumber) {
    }
}
