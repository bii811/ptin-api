package com.example.ptin.auth.domain.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class UserTemporaryPasswordTest {

    private static final SecretMatcher PLAIN = String::equals;
    private static final LockoutPolicy POLICY = new LockoutPolicy(5, Duration.ofMinutes(15));

    @Test
    void temporaryPasswordLogsInButFlagsMandatoryChangeUntilUserSetsOwn() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        User user = User.register(new MobileNumber("2012345678"));
        user.activate();

        user.assignTemporaryPassword("Temp1234", now);
        assertTrue(user.isMustChangePassword());
        assertTrue(user.authenticate("Temp1234", PLAIN, now, POLICY) == PasswordAuthenticationResult.SUCCESS);

        user.changePassword("Own12345", now.plusSeconds(5));
        assertFalse(user.isMustChangePassword());
    }
}
