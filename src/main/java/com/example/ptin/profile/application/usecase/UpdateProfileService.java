package com.example.ptin.profile.application.usecase;

import com.example.ptin.profile.domain.exception.ProfileNotFoundException;
import com.example.ptin.profile.domain.model.Profile;
import com.example.ptin.profile.domain.port.in.UpdateProfileUseCase;
import com.example.ptin.profile.domain.port.out.ProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class UpdateProfileService implements UpdateProfileUseCase {

    private final ProfileRepository profileRepository;

    UpdateProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Override
    public Profile update(UpdateProfileCommand command) {
        Profile profile = profileRepository.findByUserId(command.userId())
                .orElseThrow(() -> new ProfileNotFoundException(command.userId()));
        profile.update(command.firstName(), command.lastName(), command.avatarUrl());
        return profileRepository.save(profile);
    }
}
