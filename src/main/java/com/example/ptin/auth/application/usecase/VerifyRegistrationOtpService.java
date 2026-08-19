package com.example.ptin.auth.application.usecase;

import com.example.ptin.auth.domain.event.UserRegisteredEvent;
import com.example.ptin.auth.domain.exception.UserNotFoundException;
import com.example.ptin.auth.domain.model.MobileNumber;
import com.example.ptin.auth.domain.model.OtpPurpose;
import com.example.ptin.auth.domain.model.User;
import com.example.ptin.auth.domain.port.in.VerifyRegistrationOtpUseCase;
import com.example.ptin.auth.domain.port.out.UserRepository;
import com.example.ptin.shared.identity.UserId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class VerifyRegistrationOtpService implements VerifyRegistrationOtpUseCase {

    private final UserRepository userRepository;
    private final OtpChallengeTransactionalGateway otpVerification;
    private final ApplicationEventPublisher eventPublisher;

    VerifyRegistrationOtpService(
            UserRepository userRepository,
            OtpChallengeTransactionalGateway otpVerification,
            ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.otpVerification = otpVerification;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public UserId verifyOtp(VerifyRegistrationOtpCommand command) {
        MobileNumber mobileNumber = new MobileNumber(command.mobileNumber());
        User user = userRepository.findByMobileNumber(mobileNumber)
                .orElseThrow(() -> new UserNotFoundException(mobileNumber.toString()));

        boolean alreadyActive = user.isActive();
        otpVerification
                .verifyAndConsume(mobileNumber, OtpPurpose.REGISTRATION, command.otpCode())
                .ensureSuccess();

        user.activate();
        userRepository.save(user);

        // Only on the first activation, so a replayed verification cannot spawn a second profile.
        if (!alreadyActive) {
            eventPublisher.publishEvent(new UserRegisteredEvent(user.getId()));
        }
        return user.getId();
    }
}
