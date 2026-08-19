package com.example.ptin.auth.domain.port.in;

/** Step two of forgotten-password recovery: TIN + mobile number + OTP sets the new password. */
public interface ResetPasswordUseCase {

    void resetPassword(ResetPasswordCommand command);

    record ResetPasswordCommand(String tin, String mobileNumber, String otpCode, String newPassword) {
    }
}
