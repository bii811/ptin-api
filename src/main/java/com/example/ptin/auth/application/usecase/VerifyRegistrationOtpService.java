package com.example.ptin.auth.application.usecase;

import com.example.ptin.auth.domain.model.MobileNumber;
import com.example.ptin.auth.domain.model.OtpPurpose;
import com.example.ptin.auth.domain.port.in.FlowToken;
import com.example.ptin.auth.domain.port.in.VerifyRegistrationOtpUseCase;
import org.springframework.stereotype.Service;

/** Registration step B. The OTP is consumed, and the signed token is the only way into step C. */
@Service
class VerifyRegistrationOtpService implements VerifyRegistrationOtpUseCase {

    private final OtpChallengeTransactionalGateway otpVerification;
    private final AuthTokenIssuer tokenIssuer;

    VerifyRegistrationOtpService(OtpChallengeTransactionalGateway otpVerification, AuthTokenIssuer tokenIssuer) {
        this.otpVerification = otpVerification;
        this.tokenIssuer = tokenIssuer;
    }

    @Override
    public FlowToken verifyOtp(VerifyRegistrationOtpCommand command) {
        MobileNumber mobileNumber = new MobileNumber(command.mobileNumber());
        otpVerification
                .verifyAndConsume(mobileNumber, OtpPurpose.REGISTRATION, command.otpCode())
                .ensureSuccess();
        return tokenIssuer.issueFlowToken(OtpPurpose.REGISTRATION, mobileNumber);
    }
}
