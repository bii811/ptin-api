package com.example.ptin.auth.domain.port.in;

/** Registration step B: a valid OTP is exchanged for a registration token. */
public interface VerifyRegistrationOtpUseCase {

    FlowToken verifyOtp(VerifyRegistrationOtpCommand command);

    record VerifyRegistrationOtpCommand(String mobileNumber, String otpCode) {
    }
}
