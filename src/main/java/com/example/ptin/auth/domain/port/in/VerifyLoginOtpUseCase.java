package com.example.ptin.auth.domain.port.in;

public interface VerifyLoginOtpUseCase {

    AuthToken verifyOtp(VerifyLoginOtpCommand command);

    record VerifyLoginOtpCommand(String mobileNumber, String otpCode) {
    }
}
