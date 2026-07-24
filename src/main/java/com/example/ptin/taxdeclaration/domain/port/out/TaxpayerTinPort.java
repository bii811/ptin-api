package com.example.ptin.taxdeclaration.domain.port.out;

import com.example.ptin.shared.identity.UserId;
import java.util.Optional;

/**
 * Anti-corruption port through which {@code taxdeclaration} looks up a user's own issued PTIN
 * without depending on the {@code ptin} module's domain/persistence internals.
 */
public interface TaxpayerTinPort {

    Optional<String> findIssuedTin(UserId userId);
}
