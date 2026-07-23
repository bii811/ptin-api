package com.example.ptin.ptin.domain.port.out;

import com.example.ptin.shared.identity.UserId;

/**
 * Anti-corruption port through which {@code ptin} checks profile completeness without depending
 * on the {@code profile} module's domain/persistence internals.
 */
public interface ProfileCompletionPort {

    boolean isProfileComplete(UserId userId);
}
