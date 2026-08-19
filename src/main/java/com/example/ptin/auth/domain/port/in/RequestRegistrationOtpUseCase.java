package com.example.ptin.auth.domain.port.in;

public interface RequestRegistrationOtpUseCase {

    OtpIssued requestOtp(RequestRegistrationOtpCommand command);

    record RequestRegistrationOtpCommand(String mobileNumber) {
    }
}
