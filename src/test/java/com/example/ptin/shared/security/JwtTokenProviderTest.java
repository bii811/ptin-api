package com.example.ptin.shared.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.ptin.auth.domain.model.PasswordStrength;
import com.example.ptin.auth.domain.model.RawPassword;
import com.example.ptin.shared.identity.UserId;
import com.example.ptin.shared.security.token.JwtTokenProvider;
import io.jsonwebtoken.JwtException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

    private static final String SECRET = Base64.getEncoder().encodeToString(new byte[32]);

    private static JwtTokenProvider provider(Clock clock) {
        return new JwtTokenProvider(SECRET, 86400, 600, "ptin-api", clock);
    }

    @Test
    void flowTokenIsNotAnAccessTokenAndIsBoundToItsPurpose() {
        Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
        JwtTokenProvider tokens = provider(clock);

        String flow = tokens.issueFlowToken("REGISTRATION", "20512345678").value();
        assertEquals("20512345678", tokens.parseFlowToken(flow, "REGISTRATION").getSubject());
        assertThrows(JwtException.class, () -> tokens.parseFlowToken(flow, "PASSWORD_RESET"));
        assertThrows(JwtException.class, () -> tokens.parse(flow));

        String access = tokens.issueAccessToken(UserId.generate(), "APPLICANT").value();
        assertThrows(JwtException.class, () -> tokens.parseFlowToken(access, "REGISTRATION"));
    }

    @Test
    void flowTokenExpiresAfterTenMinutes() {
        Instant start = Instant.parse("2026-01-01T00:00:00Z");
        String flow = provider(Clock.fixed(start, ZoneOffset.UTC)).issueFlowToken("REGISTRATION", "x").value();
        JwtTokenProvider later = provider(Clock.fixed(start.plus(Duration.ofMinutes(11)), ZoneOffset.UTC));
        assertThrows(JwtException.class, () -> later.parseFlowToken(flow, "REGISTRATION"));
    }

    @Test
    void passwordStrengthIsAdvisoryAboveMinimumLength() {
        assertEquals(PasswordStrength.WEAK, new RawPassword("alllowercase").strength());
        assertEquals(PasswordStrength.STRONG, new RawPassword("Abcdefg1").strength());
        assertThrows(RuntimeException.class, () -> new RawPassword("Ab1"));
    }
}
