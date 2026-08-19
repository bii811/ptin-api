package com.example.ptin.auth.application.usecase;

import com.example.ptin.auth.config.OtpProperties;
import com.example.ptin.auth.domain.exception.InvalidCredentialsException;
import com.example.ptin.auth.domain.model.MobileNumber;
import com.example.ptin.auth.domain.model.OtpPurpose;
import com.example.ptin.auth.domain.port.in.OtpIssued;
import com.example.ptin.auth.domain.port.in.RequestPasswordResetOtpUseCase;
import com.example.ptin.auth.domain.port.out.OtpSender;
import org.springframework.stereotype.Service;

/**
 * Forgotten-password step one. The caller must supply both the TIN and the mobile number registered
 * against it; the OTP is only ever sent to the number already on the account, never to one supplied
 * by the caller.
 *
 * <p>Not {@code @Transactional} — the SMS dispatch stays outside the OTP issuer's transaction.
 */
@Service
class RequestPasswordResetOtpService implements RequestPasswordResetOtpUseCase {

    private final PasswordResetAccountResolver accountResolver;
    private final OtpChallengeIssuer otpChallengeIssuer;
    private final OtpSender otpSender;
    private final OtpProperties properties;

    RequestPasswordResetOtpService(
            PasswordResetAccountResolver accountResolver,
            OtpChallengeIssuer otpChallengeIssuer,
            OtpSender otpSender,
            OtpProperties properties) {
        this.accountResolver = accountResolver;
        this.otpChallengeIssuer = otpChallengeIssuer;
        this.otpSender = otpSender;
        this.properties = properties;
    }

    @Override
    public OtpIssued requestOtp(RequestPasswordResetOtpCommand command) {
        MobileNumber claimedNumber = new MobileNumber(command.mobileNumber());
        var account = accountResolver.resolve(command.tin(), claimedNumber);
        if (account.isEmpty()) {
            if (properties.concealAccountExistence()) {
                return OtpIssued.suppressed();
            }
            throw new InvalidCredentialsException();
        }

        MobileNumber registeredNumber = account.get().getMobileNumber();
        String plainCode = otpChallengeIssuer.issue(registeredNumber, OtpPurpose.PASSWORD_RESET);
        otpSender.send(registeredNumber, plainCode);
        return OtpIssued.sent(plainCode);
    }
}
