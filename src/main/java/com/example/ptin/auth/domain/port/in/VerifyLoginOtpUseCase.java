package com.example.ptin.auth.domain.port.in;

import java.time.Instant;

public interface VerifyLoginOtpUseCase {

    AuthToken verifyOtp(VerifyLoginOtpCommand command);

    record VerifyLoginOtpCommand(String mobileNumber, String otpCode) {
    }

    record AuthToken(String accessToken, Instant expiresAt, String role) {
    }
}
