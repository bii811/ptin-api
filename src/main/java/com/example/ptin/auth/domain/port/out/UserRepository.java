package com.example.ptin.auth.domain.port.out;

import com.example.ptin.auth.domain.model.MobileNumber;
import com.example.ptin.auth.domain.model.User;
import com.example.ptin.shared.identity.UserId;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findByMobileNumber(MobileNumber mobileNumber);

    Optional<User> findById(UserId id);

    User save(User user);
}
