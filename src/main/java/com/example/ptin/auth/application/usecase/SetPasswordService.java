package com.example.ptin.auth.application.usecase;

import com.example.ptin.auth.domain.exception.InvalidCredentialsException;
import com.example.ptin.auth.domain.exception.UserNotFoundException;
import com.example.ptin.auth.domain.model.RawPassword;
import com.example.ptin.auth.domain.model.User;
import com.example.ptin.auth.domain.port.in.SetPasswordUseCase;
import com.example.ptin.auth.domain.port.out.UserRepository;
import java.time.Clock;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Changes the password of a logged-in user. Replacing an existing password requires the current one;
 * accounts that predate password registration have none, and may set it without.
 */
@Service
@Transactional
class SetPasswordService implements SetPasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    SetPasswordService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            Clock clock) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    @Override
    public void setPassword(SetPasswordCommand command) {
        User user = userRepository.findById(command.userId())
                .filter(User::isActive)
                .orElseThrow(() -> new UserNotFoundException(command.userId().toString()));

        // Replacing an existing password requires proving you know it; the initial set does not,
        // because reaching here already required a valid OTP login.
        if (user.hasPassword() && !user.matchesCurrentPassword(command.currentPassword(), passwordEncoder::matches)) {
            throw new InvalidCredentialsException();
        }

        RawPassword newPassword = new RawPassword(command.newPassword());
        user.changePassword(passwordEncoder.encode(newPassword.value()), clock.instant());
        userRepository.save(user);
    }
}
