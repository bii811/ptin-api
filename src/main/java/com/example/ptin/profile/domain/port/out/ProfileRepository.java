package com.example.ptin.profile.domain.port.out;

import com.example.ptin.profile.domain.model.Profile;
import com.example.ptin.shared.identity.UserId;
import java.util.Optional;

public interface ProfileRepository {

    Optional<Profile> findByUserId(UserId userId);

    Profile save(Profile profile);
}
