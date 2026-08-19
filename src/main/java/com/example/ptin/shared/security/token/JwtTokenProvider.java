package com.example.ptin.shared.security.token;

import com.example.ptin.shared.identity.UserId;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    /** HS256 needs at least 256 bits of key material; anything shorter is trivially brute-forced. */
    private static final int MIN_SECRET_BYTES = 32;

    private static final String ROLE_CLAIM = "role";

    private final SecretKey signingKey;
    private final long accessTokenExpirySeconds;
    private final String issuer;
    private final Clock clock;

    public JwtTokenProvider(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.access-token-expiry-seconds}") long accessTokenExpirySeconds,
            @Value("${security.jwt.issuer}") String issuer,
            Clock clock) {
        this.signingKey = Keys.hmacShaKeyFor(decodeSecret(secret));
        this.accessTokenExpirySeconds = accessTokenExpirySeconds;
        this.issuer = issuer;
        this.clock = clock;
    }

    public IssuedToken issueAccessToken(UserId userId, String role) {
        Instant issuedAt = clock.instant().truncatedTo(ChronoUnit.SECONDS);
        Instant expiresAt = issuedAt.plusSeconds(accessTokenExpirySeconds);
        String token = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(issuer)
                .subject(userId.toString())
                .claim(ROLE_CLAIM, role)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
        return new IssuedToken(token, issuedAt, expiresAt);
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(issuer)
                .clock(() -> Date.from(clock.instant()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private static byte[] decodeSecret(String secret) {
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("security.jwt.secret must be Base64-encoded", e);
        }
        if (decoded.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "security.jwt.secret must decode to at least " + MIN_SECRET_BYTES + " bytes, got " + decoded.length);
        }
        return decoded;
    }
}
