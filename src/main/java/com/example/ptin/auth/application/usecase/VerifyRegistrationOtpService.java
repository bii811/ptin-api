package com.example.ptin.auth.application.usecase;

import com.example.ptin.auth.domain.event.UserRegisteredEvent;
import com.example.ptin.auth.domain.exception.InvalidOtpException;
import com.example.ptin.auth.domain.exception.UserNotFoundException;
import com.example.ptin.auth.domain.model.MobileNumber;
import com.example.ptin.auth.domain.model.OtpChallenge;
import com.example.ptin.auth.domain.model.OtpPurpose;
import com.example.ptin.auth.domain.model.User;
import com.example.ptin.auth.domain.port.in.VerifyRegistrationOtpUseCase;
import com.example.ptin.auth.domain.port.out.OtpChallengeRepository;
import com.example.ptin.auth.domain.port.out.UserRepository;
import com.example.ptin.shared.identity.UserId;
import java.time.Instant;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class VerifyRegistrationOtpService implements VerifyRegistrationOtpUseCase {

    private final UserRepository userRepository;
    private final OtpChallengeRepository otpChallengeRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    VerifyRegistrationOtpService(
            UserRepository userRepository,
            OtpChallengeRepository otpChallengeRepository,
            PasswordEncoder passwordEncoder,
            ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.otpChallengeRepository = otpChallengeRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public UserId verifyOtp(VerifyRegistrationOtpCommand command) {
        MobileNumber mobileNumber = new MobileNumber(command.mobileNumber());
        User user = userRepository.findByMobileNumber(mobileNumber)
                .orElseThrow(() -> new UserNotFoundException(mobileNumber.toString()));
        OtpChallenge challenge = otpChallengeRepository.findActiveChallenge(mobileNumber, OtpPurpose.REGISTRATION)
                .orElseThrow(InvalidOtpException::new);

        try {
            challenge.verify(command.otpCode(), passwordEncoder::matches, Instant.now());
        } finally {
            otpChallengeRepository.save(challenge);
        }

        user.activate();
        userRepository.save(user);

        eventPublisher.publishEvent(new UserRegisteredEvent(user.getId()));
        return user.getId();
    }
}
