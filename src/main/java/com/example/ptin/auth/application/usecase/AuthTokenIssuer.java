package com.example.ptin.auth.application.usecase;

import com.example.ptin.auth.domain.exception.InvalidFlowTokenException;
import com.example.ptin.auth.domain.model.MobileNumber;
import com.example.ptin.auth.domain.model.OtpPurpose;
import com.example.ptin.auth.domain.model.User;
import com.example.ptin.auth.domain.port.in.AuthToken;
import com.example.ptin.auth.domain.port.in.FlowToken;
import com.example.ptin.shared.security.token.IssuedToken;
import com.example.ptin.shared.security.token.JwtTokenProvider;
import io.jsonwebtoken.JwtException;
import org.springframework.stereotype.Component;

/** Single place access tokens and the registration / password-reset flow tokens are minted and redeemed. */
@Component
class AuthTokenIssuer {

    private final JwtTokenProvider jwtTokenProvider;

    AuthTokenIssuer(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    AuthToken issueFor(User user) {
        String role = user.getRole().name();
        IssuedToken token = jwtTokenProvider.issueAccessToken(user.getId(), role);
        return new AuthToken(token.value(), token.expiresAt(), role);
    }

    /** The flow is identified by the OTP purpose it follows, so a token cannot be replayed in the other flow. */
    FlowToken issueFlowToken(OtpPurpose purpose, MobileNumber mobileNumber) {
        return flowToken(purpose, mobileNumber, null);
    }

    /** Bound to the user's current password version, so it is spent by the first reset (or any password change). */
    FlowToken issuePasswordResetToken(User user) {
        return flowToken(OtpPurpose.PASSWORD_RESET, user.getMobileNumber(), user.passwordVersion());
    }

    private FlowToken flowToken(OtpPurpose purpose, MobileNumber mobileNumber, String version) {
        IssuedToken token = jwtTokenProvider.issueFlowToken(purpose.name(), mobileNumber.value(), version);
        return new FlowToken(token.value(), token.expiresAt());
    }

    RedeemedFlowToken redeemFlowToken(OtpPurpose purpose, String token) {
        try {
            var claims = jwtTokenProvider.parseFlowToken(token, purpose.name());
            return new RedeemedFlowToken(
                    new MobileNumber(claims.getSubject()), claims.get(JwtTokenProvider.VERSION_CLAIM, String.class));
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidFlowTokenException();
        }
    }

    record RedeemedFlowToken(MobileNumber mobileNumber, String version) {
    }
}
