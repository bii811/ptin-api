package com.example.ptin.auth.application.usecase;

import com.example.ptin.auth.domain.exception.OtpRequestThrottledException;
import com.example.ptin.auth.domain.model.MobileNumber;
import com.example.ptin.auth.domain.model.OtpPurpose;
import com.example.ptin.auth.domain.model.User;
import com.example.ptin.auth.domain.port.in.OtpIssued;
import com.example.ptin.auth.domain.port.in.RequestPasswordResetOtpUseCase;
import com.example.ptin.auth.domain.port.out.OtpSender;
import com.example.ptin.auth.domain.port.out.UserRepository;
import org.springframework.stereotype.Service;

/**
 * Forgotten-password step A. Always answers the same way: an unknown number gets nothing sent, and a
 * throttled known number is swallowed too, since a 429 would otherwise reveal the account exists.
 *
 * <p>Not {@code @Transactional} — the SMS dispatch stays outside the OTP issuer's transaction.
 */
@Service
class RequestPasswordResetOtpService implements RequestPasswordResetOtpUseCase {

    private final UserRepository userRepository;
    private final OtpChallengeIssuer otpChallengeIssuer;
    private final OtpSender otpSender;

    RequestPasswordResetOtpService(
            UserRepository userRepository, OtpChallengeIssuer otpChallengeIssuer, OtpSender otpSender) {
        this.userRepository = userRepository;
        this.otpChallengeIssuer = otpChallengeIssuer;
        this.otpSender = otpSender;
    }

    @Override
    public OtpIssued requestOtp(RequestPasswordResetOtpCommand command) {
        MobileNumber mobileNumber = new MobileNumber(command.mobileNumber());
        if (userRepository.findByMobileNumber(mobileNumber).filter(User::isActive).isEmpty()) {
            return OtpIssued.suppressed();
        }

        String plainCode;
        try {
            plainCode = otpChallengeIssuer.issue(mobileNumber, OtpPurpose.PASSWORD_RESET);
        } catch (OtpRequestThrottledException e) {
            return OtpIssued.suppressed();
        }
        otpSender.send(mobileNumber, plainCode);
        return OtpIssued.sent(plainCode);
    }
}
