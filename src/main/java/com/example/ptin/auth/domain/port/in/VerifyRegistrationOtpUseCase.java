package com.example.ptin.auth.domain.port.in;

import com.example.ptin.shared.identity.UserId;

public interface VerifyRegistrationOtpUseCase {

    UserId verifyOtp(VerifyRegistrationOtpCommand command);

    record VerifyRegistrationOtpCommand(String mobileNumber, String otpCode) {
    }
}
