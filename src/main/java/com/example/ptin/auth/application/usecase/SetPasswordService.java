package com.example.ptin.auth.application.usecase;

import com.example.ptin.auth.domain.exception.InvalidCredentialsException;
import com.example.ptin.auth.domain.exception.PtinRequiredException;
import com.example.ptin.auth.domain.exception.UserNotFoundException;
import com.example.ptin.auth.domain.model.RawPassword;
import com.example.ptin.auth.domain.model.User;
import com.example.ptin.auth.domain.port.in.SetPasswordUseCase;
import com.example.ptin.auth.domain.port.out.TaxpayerTinLookupPort;
import com.example.ptin.auth.domain.port.out.UserRepository;
import java.time.Clock;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Sets the password that unlocks TIN + password login. Requires an issued TIN, because the TIN is
 * the username for that login path — a password without one would be unusable.
 */
@Service
@Transactional
class SetPasswordService implements SetPasswordUseCase {

    private final UserRepository userRepository;
    private final TaxpayerTinLookupPort taxpayerTinLookup;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    SetPasswordService(
            UserRepository userRepository,
            TaxpayerTinLookupPort taxpayerTinLookup,
            PasswordEncoder passwordEncoder,
            Clock clock) {
        this.userRepository = userRepository;
        this.taxpayerTinLookup = taxpayerTinLookup;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    @Override
    public void setPassword(SetPasswordCommand command) {
        User user = userRepository.findById(command.userId())
                .filter(User::isActive)
                .orElseThrow(() -> new UserNotFoundException(command.userId().toString()));

        if (taxpayerTinLookup.findIssuedTin(user.getId()).isEmpty()) {
            throw new PtinRequiredException();
        }

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
