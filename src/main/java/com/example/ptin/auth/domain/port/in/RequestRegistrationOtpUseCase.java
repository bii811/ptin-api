package com.example.ptin.auth.domain.port.in;

public interface RequestRegistrationOtpUseCase {

    void requestOtp(RequestRegistrationOtpCommand command);

    record RequestRegistrationOtpCommand(String mobileNumber) {
    }
}
