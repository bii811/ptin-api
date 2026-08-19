package com.example.ptin.auth.domain.port.out;

import com.example.ptin.shared.identity.UserId;
import java.util.Optional;

/**
 * Anti-corruption port through which {@code auth} resolves the TIN &lt;-&gt; user mapping it needs for
 * password login, without depending on the {@code ptin} module's internals.
 */
public interface TaxpayerTinLookupPort {

    Optional<String> findIssuedTin(UserId userId);

    Optional<UserId> findUserIdByIssuedTin(String tin);
}
