package com.example.ptin.ptin.domain.port.in;

import com.example.ptin.shared.identity.UserId;
import java.util.Optional;

/**
 * Narrow read port over issued TINs, for modules that need to resolve the TIN &lt;-&gt; user mapping
 * without pulling in the whole PTIN application aggregate.
 *
 * <p>Deliberately <strong>not</strong> role-gated: {@code auth} calls
 * {@link #findUserIdByIssuedTin(String)} from the unauthenticated TIN + password login path.
 * It exposes nothing beyond "does this TIN exist, and whose is it", which the caller must
 * still pair with a credential check before acting on.
 */
public interface FindIssuedTinUseCase {

    /** The TIN issued to this user, or empty if no application of theirs has reached ISSUED. */
    Optional<String> findIssuedTinByUser(UserId userId);

    /** The owner of an issued TIN, or empty if no issued application carries it. */
    Optional<UserId> findUserIdByIssuedTin(String tin);
}
