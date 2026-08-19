package com.example.ptin.auth.application.usecase;

import com.example.ptin.auth.domain.exception.InvalidCredentialsException;
import com.example.ptin.auth.domain.port.in.AuthToken;
import com.example.ptin.auth.domain.port.in.LoginWithPasswordUseCase;
import com.example.ptin.auth.domain.port.out.TaxpayerTinLookupPort;
import com.example.ptin.shared.identity.UserId;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * TIN + password login, the path a user gets once their TIN has been issued and they have set a
 * password. Phone + OTP remains available in parallel.
 *
 * <p>Not {@code @Transactional}: {@link UserCredentialTransactionalGateway} owns the write, and it
 * must commit the lockout bookkeeping independently of the 401 this method raises.
 */
@Service
class LoginWithPasswordService implements LoginWithPasswordUseCase {

    private final TaxpayerTinLookupPort taxpayerTinLookup;
    private final UserCredentialTransactionalGateway credentials;
    private final AuthTokenIssuer authTokenIssuer;

    LoginWithPasswordService(
            TaxpayerTinLookupPort taxpayerTinLookup,
            UserCredentialTransactionalGateway credentials,
            AuthTokenIssuer authTokenIssuer) {
        this.taxpayerTinLookup = taxpayerTinLookup;
        this.credentials = credentials;
        this.authTokenIssuer = authTokenIssuer;
    }

    @Override
    public AuthToken login(LoginWithPasswordCommand command) {
        Optional<UserId> userId = taxpayerTinLookup.findUserIdByIssuedTin(command.tin());
        if (userId.isEmpty()) {
            // Same error an existing TIN with a wrong password gets, so an unknown TIN is
            // indistinguishable from a wrong password.
            throw new InvalidCredentialsException();
        }

        var attempt = credentials.authenticate(userId.get(), command.password());
        // Throws unless the attempt succeeded; the gateway has already committed the lockout counters.
        attempt.result().ensureSuccess();
        return authTokenIssuer.issueFor(attempt.user());
    }
}
