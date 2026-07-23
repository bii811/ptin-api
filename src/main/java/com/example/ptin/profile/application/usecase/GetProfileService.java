package com.example.ptin.profile.application.usecase;

import com.example.ptin.profile.domain.exception.ProfileNotFoundException;
import com.example.ptin.profile.domain.model.Profile;
import com.example.ptin.profile.domain.port.in.GetProfileUseCase;
import com.example.ptin.profile.domain.port.out.ProfileRepository;
import com.example.ptin.shared.identity.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class GetProfileService implements GetProfileUseCase {

    private final ProfileRepository profileRepository;

    GetProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Override
    public Profile getByUserId(UserId userId) {
        return profileRepository.findByUserId(userId).orElseThrow(() -> new ProfileNotFoundException(userId));
    }
}
