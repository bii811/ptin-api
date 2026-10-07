package com.example.ptin.auth.application.usecase;

import com.example.ptin.auth.domain.model.MobileNumber;
import com.example.ptin.auth.domain.model.OtpPurpose;
import com.example.ptin.auth.domain.port.in.FlowToken;
import com.example.ptin.auth.domain.port.in.VerifyPasswordResetOtpUseCase;
import org.springframework.stereotype.Service;

/**
 * Forgotten-password step B. Does not look the account up: an OTP only exists for a real account, and
 * every failure collapses into the same "invalid OTP" so this cannot be used to probe numbers.
 */
@Service
class VerifyPasswordResetOtpService implements VerifyPasswordResetOtpUseCase {

    private final OtpChallengeTransactionalGateway otpVerification;
    private final AuthTokenIssuer tokenIssuer;

    VerifyPasswordResetOtpService(OtpChallengeTransactionalGateway otpVerification, AuthTokenIssuer tokenIssuer) {
        this.otpVerification = otpVerification;
        this.tokenIssuer = tokenIssuer;
    }

    @Override
    public FlowToken verifyOtp(VerifyPasswordResetOtpCommand command) {
        MobileNumber mobileNumber = new MobileNumber(command.mobileNumber());
        otpVerification
                .verifyAndConsume(mobileNumber, OtpPurpose.PASSWORD_RESET, command.otpCode())
                .ensureSuccess();
        return tokenIssuer.issueFlowToken(OtpPurpose.PASSWORD_RESET, mobileNumber);
    }
}
