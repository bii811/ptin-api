package com.example.ptin.auth.infrastructure.acl;

import com.example.ptin.auth.domain.port.out.ProfileCompletionLookupPort;
import com.example.ptin.profile.domain.port.in.GetProfileUseCase;
import com.example.ptin.shared.identity.UserId;
import org.springframework.stereotype.Component;

@Component
class ProfileCompletionLookupAdapter implements ProfileCompletionLookupPort {

    private final GetProfileUseCase getProfileUseCase;

    ProfileCompletionLookupAdapter(GetProfileUseCase getProfileUseCase) {
        this.getProfileUseCase = getProfileUseCase;
    }

    @Override
    public boolean isProfileComplete(UserId userId) {
        return getProfileUseCase.getByUserId(userId).isComplete();
    }
}
