package com.example.ptin.auth.application.usecase;

import com.example.ptin.auth.domain.exception.InvalidOtpException;
import com.example.ptin.auth.domain.model.MobileNumber;
import com.example.ptin.auth.domain.model.OtpPurpose;
import com.example.ptin.auth.domain.model.User;
import com.example.ptin.auth.domain.port.in.FlowToken;
import com.example.ptin.auth.domain.port.in.VerifyPasswordResetOtpUseCase;
import com.example.ptin.auth.domain.port.out.UserRepository;
import org.springframework.stereotype.Service;

/**
 * Forgotten-password step B. Does not look the account up: an OTP only exists for a real account, and
 * every failure collapses into the same "invalid OTP" so this cannot be used to probe numbers.
 */
@Service
class VerifyPasswordResetOtpService implements VerifyPasswordResetOtpUseCase {

    private final OtpChallengeTransactionalGateway otpVerification;
    private final AuthTokenIssuer tokenIssuer;
    private final UserRepository userRepository;

    VerifyPasswordResetOtpService(
            OtpChallengeTransactionalGateway otpVerification, AuthTokenIssuer tokenIssuer, UserRepository userRepository) {
        this.otpVerification = otpVerification;
        this.tokenIssuer = tokenIssuer;
        this.userRepository = userRepository;
    }

    @Override
    public FlowToken verifyOtp(VerifyPasswordResetOtpCommand command) {
        MobileNumber mobileNumber = new MobileNumber(command.mobileNumber());
        otpVerification
                .verifyAndConsume(mobileNumber, OtpPurpose.PASSWORD_RESET, command.otpCode())
                .ensureSuccess();
        // An OTP only exists for a real account; a missing one is reported like a bad code.
        User user = userRepository.findByMobileNumber(mobileNumber).orElseThrow(InvalidOtpException::new);
        return tokenIssuer.issuePasswordResetToken(user);
    }
}
