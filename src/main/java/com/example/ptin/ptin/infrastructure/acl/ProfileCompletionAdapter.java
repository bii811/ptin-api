package com.example.ptin.ptin.infrastructure.acl;

import com.example.ptin.profile.domain.exception.ProfileNotFoundException;
import com.example.ptin.profile.domain.port.in.GetProfileUseCase;
import com.example.ptin.ptin.domain.port.out.ProfileCompletionPort;
import com.example.ptin.shared.identity.UserId;
import org.springframework.stereotype.Component;

@Component
class ProfileCompletionAdapter implements ProfileCompletionPort {

    private final GetProfileUseCase getProfileUseCase;

    ProfileCompletionAdapter(GetProfileUseCase getProfileUseCase) {
        this.getProfileUseCase = getProfileUseCase;
    }

    @Override
    public boolean isProfileComplete(UserId userId) {
        try {
            return getProfileUseCase.getByUserId(userId).isComplete();
        } catch (ProfileNotFoundException e) {
            return false;
        }
    }
}
