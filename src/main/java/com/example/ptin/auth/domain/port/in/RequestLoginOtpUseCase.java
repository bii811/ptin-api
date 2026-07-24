package com.example.ptin.auth.domain.port.in;

public interface RequestLoginOtpUseCase {

    OtpIssued requestOtp(RequestLoginOtpCommand command);

    record RequestLoginOtpCommand(String mobileNumber) {
    }

    record OtpIssued(String otpCode) {
    }
}
