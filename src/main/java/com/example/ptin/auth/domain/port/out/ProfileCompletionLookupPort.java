package com.example.ptin.auth.domain.port.out;

import com.example.ptin.shared.identity.UserId;

/**
 * Anti-corruption port letting {@code auth} report profile completeness on {@code /me} without
 * reaching into the {@code profile} module's internals.
 */
public interface ProfileCompletionLookupPort {

    boolean isProfileComplete(UserId userId);
}
