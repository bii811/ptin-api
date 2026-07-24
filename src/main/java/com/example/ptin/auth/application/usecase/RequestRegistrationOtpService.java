package com.example.ptin.auth.application.usecase;

import com.example.ptin.auth.domain.exception.UserAlreadyRegisteredException;
import com.example.ptin.auth.domain.model.MobileNumber;
import com.example.ptin.auth.domain.model.OtpChallenge;
import com.example.ptin.auth.domain.model.OtpPurpose;
import com.example.ptin.auth.domain.model.User;
import com.example.ptin.auth.domain.port.in.RequestRegistrationOtpUseCase;
import com.example.ptin.auth.domain.port.out.OtpChallengeRepository;
import com.example.ptin.auth.domain.port.out.OtpSender;
import com.example.ptin.auth.domain.port.out.UserRepository;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class RequestRegistrationOtpService implements RequestRegistrationOtpUseCase {

    private final UserRepository userRepository;
    private final OtpChallengeRepository otpChallengeRepository;
    private final OtpSender otpSender;
    private final OtpCodeGenerator otpCodeGenerator;
    private final PasswordEncoder passwordEncoder;
    private final long otpTtlSeconds;
    private final int otpMaxAttempts;

    RequestRegistrationOtpService(
            UserRepository userRepository,
            OtpChallengeRepository otpChallengeRepository,
            OtpSender otpSender,
            OtpCodeGenerator otpCodeGenerator,
            PasswordEncoder passwordEncoder,
            @Value("${otp.ttl-seconds}") long otpTtlSeconds,
            @Value("${otp.max-attempts}") int otpMaxAttempts) {
        this.userRepository = userRepository;
        this.otpChallengeRepository = otpChallengeRepository;
        this.otpSender = otpSender;
        this.otpCodeGenerator = otpCodeGenerator;
        this.passwordEncoder = passwordEncoder;
        this.otpTtlSeconds = otpTtlSeconds;
        this.otpMaxAttempts = otpMaxAttempts;
    }

    @Override
    public OtpIssued requestOtp(RequestRegistrationOtpCommand command) {
        MobileNumber mobileNumber = new MobileNumber(command.mobileNumber());
        User user = userRepository.findByMobileNumber(mobileNumber).orElseGet(() -> User.register(mobileNumber));
        if (user.isActive()) {
            throw new UserAlreadyRegisteredException(mobileNumber.toString());
        }
        userRepository.save(user);

        String plainCode = otpCodeGenerator.generate();
        String hashedCode = passwordEncoder.encode(plainCode);
        OtpChallenge challenge = OtpChallenge.issue(
                mobileNumber, OtpPurpose.REGISTRATION, hashedCode, Instant.now().plusSeconds(otpTtlSeconds), otpMaxAttempts);
        otpChallengeRepository.save(challenge);

        otpSender.send(mobileNumber, plainCode);
        return new OtpIssued(plainCode);
    }
}
