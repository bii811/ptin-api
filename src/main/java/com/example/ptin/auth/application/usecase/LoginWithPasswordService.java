package com.example.ptin.auth.application.usecase;

import com.example.ptin.auth.domain.exception.InvalidCredentialsException;
import com.example.ptin.auth.domain.model.MobileNumber;
import com.example.ptin.auth.domain.model.OtpPurpose;
import com.example.ptin.auth.domain.model.User;
import com.example.ptin.auth.domain.port.in.LoginResult;
import com.example.ptin.auth.domain.port.in.LoginWithPasswordUseCase;
import com.example.ptin.auth.domain.port.out.TaxpayerTinLookupPort;
import com.example.ptin.auth.domain.port.out.UserRepository;
import com.example.ptin.shared.identity.UserId;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Password login by mobile number, or by TIN once one has been issued to the account.
 *
 * <p>Not {@code @Transactional}: {@link UserCredentialTransactionalGateway} owns the write, and it
 * must commit the lockout bookkeeping independently of the error this method raises.
 */
@Service
class LoginWithPasswordService implements LoginWithPasswordUseCase {

    private final UserRepository userRepository;
    private final TaxpayerTinLookupPort taxpayerTinLookup;
    private final UserCredentialTransactionalGateway credentials;
    private final AuthTokenIssuer authTokenIssuer;
    private final PasswordEncoder passwordEncoder;
    /** Matched against when the account is unknown, so a miss costs as much as a real check. */
    private final String decoyHash;

    LoginWithPasswordService(
            UserRepository userRepository,
            TaxpayerTinLookupPort taxpayerTinLookup,
            UserCredentialTransactionalGateway credentials,
            AuthTokenIssuer authTokenIssuer,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.taxpayerTinLookup = taxpayerTinLookup;
        this.credentials = credentials;
        this.authTokenIssuer = authTokenIssuer;
        this.passwordEncoder = passwordEncoder;
        this.decoyHash = passwordEncoder.encode("decoy-password-never-matches");
    }

    @Override
    public LoginResult login(LoginWithPasswordCommand command) {
        Optional<UserId> userId = resolve(command);
        if (userId.isEmpty()) {
            // Same error, and similar timing, as a wrong password on a real account.
            passwordEncoder.matches(command.password(), decoyHash);
            throw new InvalidCredentialsException();
        }

        var attempt = credentials.authenticate(userId.get(), command.password());
        // Throws unless the attempt succeeded; the gateway has already committed the lockout counters.
        attempt.result().ensureSuccess();
        User user = attempt.user();
        if (user.isMustChangePassword()) {
            // Temporary password: no session until the user picks their own (spent on /forgot-password/reset).
            return LoginResult.passwordChangeRequired(
                    authTokenIssuer.issueFlowToken(OtpPurpose.PASSWORD_RESET, user.getMobileNumber()));
        }
        return LoginResult.authenticated(authTokenIssuer.issueFor(user));
    }

    private Optional<UserId> resolve(LoginWithPasswordCommand command) {
        boolean hasMobile = command.mobileNumber() != null && !command.mobileNumber().isBlank();
        boolean hasTin = command.tin() != null && !command.tin().isBlank();
        if (hasMobile == hasTin) {
            throw new IllegalArgumentException("Provide either mobileNumber or tin");
        }
        if (hasTin) {
            return taxpayerTinLookup.findUserIdByIssuedTin(command.tin());
        }
        return userRepository.findByMobileNumber(new MobileNumber(command.mobileNumber())).map(User::getId);
    }
}
