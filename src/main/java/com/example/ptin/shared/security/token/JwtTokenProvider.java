package com.example.ptin.shared.security.token;

import com.example.ptin.shared.identity.UserId;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
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
    private static final String PURPOSE_CLAIM = "purpose";

    private final SecretKey signingKey;
    private final long accessTokenExpirySeconds;
    private final long flowTokenExpirySeconds;
    private final String issuer;
    private final Clock clock;

    public JwtTokenProvider(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.access-token-expiry-seconds}") long accessTokenExpirySeconds,
            @Value("${security.jwt.flow-token-expiry-seconds}") long flowTokenExpirySeconds,
            @Value("${security.jwt.issuer}") String issuer,
            Clock clock) {
        this.signingKey = Keys.hmacShaKeyFor(decodeSecret(secret));
        this.accessTokenExpirySeconds = accessTokenExpirySeconds;
        this.flowTokenExpirySeconds = flowTokenExpirySeconds;
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

    /**
     * Short-lived token proving one step of a multi-step flow was passed. Carries a {@code purpose}
     * claim and no role, so it can never be accepted as an access token (see {@link #parse}).
     */
    public IssuedToken issueFlowToken(String purpose, String subject) {
        Instant issuedAt = clock.instant().truncatedTo(ChronoUnit.SECONDS);
        Instant expiresAt = issuedAt.plusSeconds(flowTokenExpirySeconds);
        String token = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(issuer)
                .subject(subject)
                .claim(PURPOSE_CLAIM, purpose)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
        return new IssuedToken(token, issuedAt, expiresAt);
    }

    /** Parses an access token; flow tokens are rejected. */
    public Claims parse(String token) {
        Claims claims = parseSigned(token);
        if (claims.get(PURPOSE_CLAIM) != null) {
            throw new JwtException("Flow token is not an access token");
        }
        return claims;
    }

    /** Parses a flow token issued for exactly {@code purpose}; access tokens are rejected. */
    public Claims parseFlowToken(String token, String purpose) {
        Claims claims = parseSigned(token);
        if (!purpose.equals(claims.get(PURPOSE_CLAIM, String.class))) {
            throw new JwtException("Token purpose mismatch");
        }
        return claims;
    }

    private Claims parseSigned(String token) {
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
