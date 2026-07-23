package com.example.ptin.auth.application.usecase;

import com.example.ptin.auth.domain.exception.InvalidOtpException;
import com.example.ptin.auth.domain.exception.UserNotFoundException;
import com.example.ptin.auth.domain.model.MobileNumber;
import com.example.ptin.auth.domain.model.OtpChallenge;
import com.example.ptin.auth.domain.model.OtpPurpose;
import com.example.ptin.auth.domain.model.User;
import com.example.ptin.auth.domain.port.in.VerifyLoginOtpUseCase;
import com.example.ptin.auth.domain.port.out.OtpChallengeRepository;
import com.example.ptin.auth.domain.port.out.UserRepository;
import com.example.ptin.shared.security.token.JwtTokenProvider;
import java.time.Instant;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class VerifyLoginOtpService implements VerifyLoginOtpUseCase {

    private final UserRepository userRepository;
    private final OtpChallengeRepository otpChallengeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    VerifyLoginOtpService(
            UserRepository userRepository,
            OtpChallengeRepository otpChallengeRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.otpChallengeRepository = otpChallengeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public AuthToken verifyOtp(VerifyLoginOtpCommand command) {
        MobileNumber mobileNumber = new MobileNumber(command.mobileNumber());
        User user = userRepository.findByMobileNumber(mobileNumber)
                .filter(User::isActive)
                .orElseThrow(() -> new UserNotFoundException(mobileNumber.toString()));
        OtpChallenge challenge = otpChallengeRepository.findActiveChallenge(mobileNumber, OtpPurpose.LOGIN)
                .orElseThrow(InvalidOtpException::new);

        try {
            challenge.verify(command.otpCode(), passwordEncoder::matches, Instant.now());
        } finally {
            otpChallengeRepository.save(challenge);
        }

        String role = user.getRole().name();
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), role);
        Instant expiresAt = Instant.now().plusSeconds(jwtTokenProvider.getAccessTokenExpirySeconds());
        return new AuthToken(accessToken, expiresAt, role);
    }
}
