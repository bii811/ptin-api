package com.example.ptin.auth.application.usecase;

import com.example.ptin.auth.domain.exception.UserAlreadyRegisteredException;
import com.example.ptin.auth.domain.model.MobileNumber;
import com.example.ptin.auth.domain.model.OtpPurpose;
import com.example.ptin.auth.domain.model.User;
import com.example.ptin.auth.domain.port.out.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates the placeholder (PENDING_VERIFICATION) user and its registration OTP in one transaction,
 * so a throttled request rolls the placeholder back rather than leaving an orphan row — otherwise an
 * unauthenticated caller could grow the users table one request at a time.
 *
 * <p>A separate bean from {@link RequestRegistrationOtpService} so the OTP dispatch there happens
 * after this commits, outside any transaction, and so {@code @Transactional} goes through a real
 * proxy instead of being skipped by self-invocation.
 */
@Component
class RegistrationTransactionalGateway {

    private final UserRepository userRepository;
    private final OtpChallengeIssuer otpChallengeIssuer;

    RegistrationTransactionalGateway(UserRepository userRepository, OtpChallengeIssuer otpChallengeIssuer) {
        this.userRepository = userRepository;
        this.otpChallengeIssuer = otpChallengeIssuer;
    }

    /** @return the plaintext OTP, for the caller to dispatch once this transaction has committed */
    @Transactional
    public String registerPendingUserAndIssueOtp(MobileNumber mobileNumber) {
        User user = userRepository.findByMobileNumber(mobileNumber).orElseGet(() -> User.register(mobileNumber));
        if (user.isActive()) {
            // Kept explicit rather than concealed: the client needs to route the user to login, and
            // the login endpoint already refuses to confirm whether a number is registered.
            throw new UserAlreadyRegisteredException(mobileNumber.toString());
        }

        // Joins this transaction, so hitting the throttle also rolls back the placeholder user above.
        String plainCode = otpChallengeIssuer.issue(mobileNumber, OtpPurpose.REGISTRATION);
        userRepository.save(user);
        return plainCode;
    }
}
