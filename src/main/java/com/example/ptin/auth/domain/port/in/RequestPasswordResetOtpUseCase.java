package com.example.ptin.auth.domain.port.in;

/** Step one of forgotten-password recovery: TIN + registered mobile number, answered with an OTP. */
public interface RequestPasswordResetOtpUseCase {

    OtpIssued requestOtp(RequestPasswordResetOtpCommand command);

    record RequestPasswordResetOtpCommand(String tin, String mobileNumber) {
    }
}
