package com.example.ptin.auth.application.usecase;

import com.example.ptin.auth.domain.model.MobileNumber;
import com.example.ptin.auth.domain.model.User;
import com.example.ptin.auth.domain.port.out.TaxpayerTinLookupPort;
import com.example.ptin.auth.domain.port.out.UserRepository;
import com.example.ptin.shared.identity.UserId;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Resolves the TIN + mobile-number pair used by both forgotten-password steps to a single active
 * account, returning empty whenever anything fails to line up. Shared so the two steps cannot drift
 * apart on what counts as a valid pair.
 */
@Component
class PasswordResetAccountResolver {

    private final TaxpayerTinLookupPort taxpayerTinLookup;
    private final UserRepository userRepository;

    PasswordResetAccountResolver(TaxpayerTinLookupPort taxpayerTinLookup, UserRepository userRepository) {
        this.taxpayerTinLookup = taxpayerTinLookup;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Optional<User> resolve(String tin, MobileNumber claimedMobileNumber) {
        Optional<UserId> ownerId = taxpayerTinLookup.findUserIdByIssuedTin(tin);
        if (ownerId.isEmpty()) {
            return Optional.empty();
        }
        return userRepository.findById(ownerId.get())
                .filter(User::isActive)
                // The number supplied must be the one already registered — otherwise anyone knowing a
                // TIN could have the reset code delivered to a handset they control.
                .filter(user -> user.getMobileNumber().equals(claimedMobileNumber));
    }
}
