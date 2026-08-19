package com.example.ptin.auth.domain.port.in;

/**
 * Result of an OTP request. {@code otpCode} is only populated so non-production environments without
 * a real SMS gateway can echo it back (see {@code otp.expose-in-response}); it is null when the
 * request was intentionally answered without issuing anything — e.g. an unknown account under
 * {@code otp.conceal-account-existence}.
 */
public record OtpIssued(String otpCode) {

    public static OtpIssued sent(String otpCode) {
        return new OtpIssued(otpCode);
    }

    /** No code was issued, but the caller must not be able to tell. */
    public static OtpIssued suppressed() {
        return new OtpIssued(null);
    }
}
