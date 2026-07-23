package com.example.ptin.auth.domain.port.in;

public interface RequestLoginOtpUseCase {

    void requestOtp(RequestLoginOtpCommand command);

    record RequestLoginOtpCommand(String mobileNumber) {
    }
}
