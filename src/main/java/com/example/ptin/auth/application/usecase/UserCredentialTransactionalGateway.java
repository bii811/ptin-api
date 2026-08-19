package com.example.ptin.auth.application.usecase;

import com.example.ptin.auth.config.PasswordLoginProperties;
import com.example.ptin.auth.domain.model.PasswordAuthenticationResult;
import com.example.ptin.auth.domain.model.User;
import com.example.ptin.auth.domain.port.out.UserRepository;
import com.example.ptin.shared.identity.UserId;
import java.time.Clock;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Password check plus lockout bookkeeping, committed independently of the caller — same reasoning as
 * {@link OtpChallengeTransactionalGateway}: a wrong password must durably increment
 * {@code failed_login_attempts} even though the request itself ends in a 401 rollback, and the
 * method therefore reports the outcome instead of throwing it.
 */
@Component
class UserCredentialTransactionalGateway {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordLoginProperties properties;
    private final Clock clock;

    UserCredentialTransactionalGateway(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            PasswordLoginProperties properties,
            Clock clock) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuthenticationAttempt authenticate(UserId userId, String rawPassword) {
        Optional<User> locked = userRepository.lockById(userId);
        if (locked.isEmpty()) {
            return new AuthenticationAttempt(PasswordAuthenticationResult.BAD_CREDENTIALS, null);
        }

        User user = locked.get();
        PasswordAuthenticationResult result =
                user.authenticate(rawPassword, passwordEncoder::matches, clock.instant(), properties.lockoutPolicy());
        userRepository.save(user);
        return new AuthenticationAttempt(result, result == PasswordAuthenticationResult.SUCCESS ? user : null);
    }

    /** {@code user} is populated only on success, so a failed attempt cannot leak account state. */
    record AuthenticationAttempt(PasswordAuthenticationResult result, User user) {
    }
}
