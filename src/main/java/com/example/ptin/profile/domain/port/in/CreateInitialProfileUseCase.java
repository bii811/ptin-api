package com.example.ptin.profile.domain.port.in;

import com.example.ptin.profile.domain.model.Profile;
import com.example.ptin.shared.identity.UserId;

public interface CreateInitialProfileUseCase {

    Profile createEmpty(UserId userId);
}
