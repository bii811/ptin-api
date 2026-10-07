package com.example.ptin.auth.application.usecase;

import com.example.ptin.auth.domain.exception.UserAlreadyRegisteredException;
import com.example.ptin.auth.domain.model.MobileNumber;
import com.example.ptin.auth.domain.model.OtpPurpose;
import com.example.ptin.auth.domain.model.User;
import com.example.ptin.auth.domain.port.in.OtpIssued;
import com.example.ptin.auth.domain.port.in.RequestRegistrationOtpUseCase;
import com.example.ptin.auth.domain.port.out.OtpSender;
import com.example.ptin.auth.domain.port.out.UserRepository;
import org.springframework.stereotype.Service;

/**
 * Registration step A. Nothing but the OTP challenge is persisted — the account is only created in
 * step C — so an unauthenticated caller cannot grow the users table.
 *
 * <p>Deliberately not {@code @Transactional}: the SMS dispatch must not pin a database connection.
 */
@Service
class RequestRegistrationOtpService implements RequestRegistrationOtpUseCase {

    private final UserRepository userRepository;
    private final OtpChallengeIssuer otpChallengeIssuer;
    private final OtpSender otpSender;

    RequestRegistrationOtpService(
            UserRepository userRepository, OtpChallengeIssuer otpChallengeIssuer, OtpSender otpSender) {
        this.userRepository = userRepository;
        this.otpChallengeIssuer = otpChallengeIssuer;
        this.otpSender = otpSender;
    }

    @Override
    public OtpIssued requestOtp(RequestRegistrationOtpCommand command) {
        MobileNumber mobileNumber = new MobileNumber(command.mobileNumber());
        if (userRepository.findByMobileNumber(mobileNumber).filter(User::isActive).isPresent()) {
            throw new UserAlreadyRegisteredException(mobileNumber.toString());
        }

        String plainCode = otpChallengeIssuer.issue(mobileNumber, OtpPurpose.REGISTRATION);
        otpSender.send(mobileNumber, plainCode);
        return OtpIssued.sent(plainCode);
    }
}
