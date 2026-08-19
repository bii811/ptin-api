package com.example.ptin.auth.application.usecase;

import com.example.ptin.auth.domain.exception.InvalidCredentialsException;
import com.example.ptin.auth.domain.model.MobileNumber;
import com.example.ptin.auth.domain.model.OtpPurpose;
import com.example.ptin.auth.domain.model.RawPassword;
import com.example.ptin.auth.domain.model.User;
import com.example.ptin.auth.domain.port.in.ResetPasswordUseCase;
import com.example.ptin.auth.domain.port.out.UserRepository;
import java.time.Clock;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Forgotten-password step two: TIN + registered mobile number + OTP installs a new password. Also
 * clears any standing lockout, so a user who was locked out by an attacker can recover immediately.
 */
@Service
@Transactional
class ResetPasswordService implements ResetPasswordUseCase {

    private final PasswordResetAccountResolver accountResolver;
    private final OtpChallengeTransactionalGateway otpVerification;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    ResetPasswordService(
            PasswordResetAccountResolver accountResolver,
            OtpChallengeTransactionalGateway otpVerification,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            Clock clock) {
        this.accountResolver = accountResolver;
        this.otpVerification = otpVerification;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    @Override
    public void resetPassword(ResetPasswordCommand command) {
        MobileNumber claimedNumber = new MobileNumber(command.mobileNumber());
        // Validate the new password before spending the OTP, so a policy rejection does not force the
        // user to request a fresh code.
        RawPassword newPassword = new RawPassword(command.newPassword());

        User user = accountResolver.resolve(command.tin(), claimedNumber).orElseThrow(InvalidCredentialsException::new);
        otpVerification
                .verifyAndConsume(user.getMobileNumber(), OtpPurpose.PASSWORD_RESET, command.otpCode())
                .ensureSuccess();

        user.changePassword(passwordEncoder.encode(newPassword.value()), clock.instant());
        userRepository.save(user);
    }
}
