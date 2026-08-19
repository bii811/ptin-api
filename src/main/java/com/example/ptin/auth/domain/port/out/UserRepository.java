package com.example.ptin.auth.domain.port.out;

import com.example.ptin.auth.domain.model.MobileNumber;
import com.example.ptin.auth.domain.model.User;
import com.example.ptin.shared.identity.UserId;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findByMobileNumber(MobileNumber mobileNumber);

    Optional<User> findById(UserId id);

    /**
     * Loads a user under a write lock so concurrent password attempts serialise on the failed-attempt
     * counter instead of racing. Must be called inside a transaction.
     */
    Optional<User> lockById(UserId id);

    User save(User user);
}
