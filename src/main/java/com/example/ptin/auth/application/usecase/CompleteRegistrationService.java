package com.example.ptin.auth.application.usecase;

import com.example.ptin.auth.domain.event.UserRegisteredEvent;
import com.example.ptin.auth.domain.exception.UserAlreadyRegisteredException;
import com.example.ptin.auth.domain.model.MobileNumber;
import com.example.ptin.auth.domain.model.OtpPurpose;
import com.example.ptin.auth.domain.model.PasswordStrength;
import com.example.ptin.auth.domain.model.RawPassword;
import com.example.ptin.auth.domain.model.User;
import com.example.ptin.auth.domain.port.in.CompleteRegistrationUseCase;
import com.example.ptin.auth.domain.port.out.UserRepository;
import java.time.Clock;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registration step C. The registration token is what proves the number was OTP-verified; replaying
 * it after the account exists fails with "already registered".
 */
@Service
@Transactional
class CompleteRegistrationService implements CompleteRegistrationUseCase {

    private final UserRepository userRepository;
    private final AuthTokenIssuer tokenIssuer;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    CompleteRegistrationService(
            UserRepository userRepository,
            AuthTokenIssuer tokenIssuer,
            PasswordEncoder passwordEncoder,
            ApplicationEventPublisher eventPublisher,
            Clock clock) {
        this.userRepository = userRepository;
        this.tokenIssuer = tokenIssuer;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Override
    public PasswordStrength complete(CompleteRegistrationCommand command) {
        MobileNumber mobileNumber =
                tokenIssuer.redeemFlowToken(OtpPurpose.REGISTRATION, command.registrationToken()).mobileNumber();
        RawPassword password = new RawPassword(command.password());

        // A PENDING_VERIFICATION row can exist from the earlier register-then-verify flow; adopt it.
        User user = userRepository.findByMobileNumber(mobileNumber).orElseGet(() -> User.register(mobileNumber));
        if (user.isActive()) {
            throw new UserAlreadyRegisteredException(mobileNumber.toString());
        }
        user.changePassword(passwordEncoder.encode(password.value()), clock.instant());
        user.activate();
        userRepository.save(user);

        eventPublisher.publishEvent(new UserRegisteredEvent(user.getId()));
        return password.strength();
    }
}
