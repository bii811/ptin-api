package com.example.ptin.auth.application.usecase;

import com.example.ptin.auth.config.OtpProperties;
import com.example.ptin.auth.domain.exception.UserNotFoundException;
import com.example.ptin.auth.domain.model.MobileNumber;
import com.example.ptin.auth.domain.model.OtpPurpose;
import com.example.ptin.auth.domain.model.User;
import com.example.ptin.auth.domain.port.in.OtpIssued;
import com.example.ptin.auth.domain.port.in.RequestLoginOtpUseCase;
import com.example.ptin.auth.domain.port.out.OtpSender;
import com.example.ptin.auth.domain.port.out.UserRepository;
import org.springframework.stereotype.Service;

/**
 * Phone + OTP login. This stays available for every account — it is the only credential a user has
 * before a TIN is issued, and it remains the fallback afterwards for anyone who never set a password.
 *
 * <p>Deliberately not {@code @Transactional}: the only write happens inside
 * {@link OtpChallengeIssuer#issue}, and keeping the SMS dispatch out of a transaction stops an SMS
 * round-trip from pinning a database connection.
 */
@Service
class RequestLoginOtpService implements RequestLoginOtpUseCase {

    private final UserRepository userRepository;
    private final OtpChallengeIssuer otpChallengeIssuer;
    private final OtpSender otpSender;
    private final OtpProperties properties;

    RequestLoginOtpService(
            UserRepository userRepository,
            OtpChallengeIssuer otpChallengeIssuer,
            OtpSender otpSender,
            OtpProperties properties) {
        this.userRepository = userRepository;
        this.otpChallengeIssuer = otpChallengeIssuer;
        this.otpSender = otpSender;
        this.properties = properties;
    }

    @Override
    public OtpIssued requestOtp(RequestLoginOtpCommand command) {
        MobileNumber mobileNumber = new MobileNumber(command.mobileNumber());
        boolean accountExists =
                userRepository.findByMobileNumber(mobileNumber).filter(User::isActive).isPresent();
        if (!accountExists) {
            if (properties.concealAccountExistence()) {
                // Answer exactly as we would for a real account, so this endpoint cannot be used to
                // enumerate which mobile numbers are registered.
                return OtpIssued.suppressed();
            }
            throw new UserNotFoundException(mobileNumber.toString());
        }

        String plainCode = otpChallengeIssuer.issue(mobileNumber, OtpPurpose.LOGIN);
        otpSender.send(mobileNumber, plainCode);
        return OtpIssued.sent(plainCode);
    }
}
