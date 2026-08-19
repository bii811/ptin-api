package com.example.ptin.auth.application.usecase;

import com.example.ptin.auth.domain.model.User;
import com.example.ptin.auth.domain.port.in.AuthToken;
import com.example.ptin.shared.security.token.IssuedToken;
import com.example.ptin.shared.security.token.JwtTokenProvider;
import org.springframework.stereotype.Component;

/** Single place both login paths (OTP and TIN + password) mint an access token. */
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
}
