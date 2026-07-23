package com.example.ptin.auth.infrastructure.otp;

import com.example.ptin.auth.domain.model.MobileNumber;
import com.example.ptin.auth.domain.port.out.OtpSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Stub adapter: logs the OTP code instead of dispatching a real SMS. Swap for a real
 * gateway-backed implementation of {@link OtpSender} when one is available.
 */
@Component
public class LoggingOtpSenderAdapter implements OtpSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingOtpSenderAdapter.class);

    @Override
    public void send(MobileNumber mobileNumber, String plainOtpCode) {
        log.info("OTP for {}: {}", mobileNumber, plainOtpCode);
    }
}
