package com.example.ptin.auth.domain.port.in;

/** Forgotten-password step B: a valid OTP is exchanged for a password-reset token. */
public interface VerifyPasswordResetOtpUseCase {

    FlowToken verifyOtp(VerifyPasswordResetOtpCommand command);

    record VerifyPasswordResetOtpCommand(String mobileNumber, String otpCode) {
    }
}
