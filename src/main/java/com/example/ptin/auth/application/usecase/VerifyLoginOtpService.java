package com.example.ptin.auth.application.usecase;

import com.example.ptin.auth.domain.exception.InvalidOtpException;
import com.example.ptin.auth.domain.model.MobileNumber;
import com.example.ptin.auth.domain.model.OtpPurpose;
import com.example.ptin.auth.domain.model.User;
import com.example.ptin.auth.domain.port.in.AuthToken;
import com.example.ptin.auth.domain.port.in.VerifyLoginOtpUseCase;
import com.example.ptin.auth.domain.port.out.UserRepository;
import org.springframework.stereotype.Service;

@Service
class VerifyLoginOtpService implements VerifyLoginOtpUseCase {

    private final UserRepository userRepository;
    private final OtpChallengeTransactionalGateway otpVerification;
    private final AuthTokenIssuer authTokenIssuer;

    VerifyLoginOtpService(
            UserRepository userRepository,
            OtpChallengeTransactionalGateway otpVerification,
            AuthTokenIssuer authTokenIssuer) {
        this.userRepository = userRepository;
        this.otpVerification = otpVerification;
        this.authTokenIssuer = authTokenIssuer;
    }

    @Override
    public AuthToken verifyOtp(VerifyLoginOtpCommand command) {
        MobileNumber mobileNumber = new MobileNumber(command.mobileNumber());

        // Verify first, then look the account up: failing on a missing account before the OTP check
        // would turn this endpoint into a registration oracle.
        otpVerification.verifyAndConsume(mobileNumber, OtpPurpose.LOGIN, command.otpCode()).ensureSuccess();

        User user = userRepository.findByMobileNumber(mobileNumber)
                .filter(User::isActive)
                .orElseThrow(InvalidOtpException::new);
        return authTokenIssuer.issueFor(user);
    }
}
