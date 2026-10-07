package com.example.ptin.auth.application.usecase;

import com.example.ptin.auth.domain.exception.InvalidFlowTokenException;
import com.example.ptin.auth.domain.model.OtpPurpose;
import com.example.ptin.auth.domain.model.PasswordStrength;
import com.example.ptin.auth.domain.model.RawPassword;
import com.example.ptin.auth.domain.model.User;
import com.example.ptin.auth.domain.port.in.ResetPasswordUseCase;
import com.example.ptin.auth.domain.port.out.UserRepository;
import java.time.Clock;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Forgotten-password step C. Also clears any standing lockout, so a user locked out by an attacker
 * can recover immediately.
 */
@Service
@Transactional
class ResetPasswordService implements ResetPasswordUseCase {

    private final UserRepository userRepository;
    private final AuthTokenIssuer tokenIssuer;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    ResetPasswordService(
            UserRepository userRepository, AuthTokenIssuer tokenIssuer, PasswordEncoder passwordEncoder, Clock clock) {
        this.userRepository = userRepository;
        this.tokenIssuer = tokenIssuer;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    @Override
    public PasswordStrength resetPassword(ResetPasswordCommand command) {
        var redeemed = tokenIssuer.redeemFlowToken(OtpPurpose.PASSWORD_RESET, command.resetToken());
        RawPassword newPassword = new RawPassword(command.newPassword());

        User user = userRepository.findByMobileNumber(redeemed.mobileNumber())
                .filter(User::isActive)
                .orElseThrow(InvalidFlowTokenException::new);
        // Single use: a token issued before the last password change has already been spent (or
        // superseded), so a leaked token cannot reset the password a second time.
        if (user.getPasswordUpdatedAt() != null && !redeemed.issuedAt().isAfter(user.getPasswordUpdatedAt())) {
            throw new InvalidFlowTokenException();
        }

        user.changePassword(passwordEncoder.encode(newPassword.value()), clock.instant());
        userRepository.save(user);
        return newPassword.strength();
    }
}
