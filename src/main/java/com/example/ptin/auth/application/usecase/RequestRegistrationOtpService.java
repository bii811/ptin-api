package com.example.ptin.auth.application.usecase;

import com.example.ptin.auth.domain.model.MobileNumber;
import com.example.ptin.auth.domain.port.in.OtpIssued;
import com.example.ptin.auth.domain.port.in.RequestRegistrationOtpUseCase;
import com.example.ptin.auth.domain.port.out.OtpSender;
import org.springframework.stereotype.Service;

/**
 * Deliberately not {@code @Transactional}: all persistence lives in
 * {@link RegistrationTransactionalGateway}, and the code is dispatched only once that has committed,
 * so an SMS round-trip never holds a database connection open.
 */
@Service
class RequestRegistrationOtpService implements RequestRegistrationOtpUseCase {

    private final RegistrationTransactionalGateway registration;
    private final OtpSender otpSender;

    RequestRegistrationOtpService(RegistrationTransactionalGateway registration, OtpSender otpSender) {
        this.registration = registration;
        this.otpSender = otpSender;
    }

    @Override
    public OtpIssued requestOtp(RequestRegistrationOtpCommand command) {
        MobileNumber mobileNumber = new MobileNumber(command.mobileNumber());
        String plainCode = registration.registerPendingUserAndIssueOtp(mobileNumber);

        otpSender.send(mobileNumber, plainCode);
        return OtpIssued.sent(plainCode);
    }
}
