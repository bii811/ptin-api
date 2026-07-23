package com.example.ptin.profile.application.usecase;

import com.example.ptin.profile.domain.model.Profile;
import com.example.ptin.profile.domain.port.in.CreateInitialProfileUseCase;
import com.example.ptin.profile.domain.port.out.ProfileRepository;
import com.example.ptin.shared.identity.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
class CreateInitialProfileService implements CreateInitialProfileUseCase {

    private final ProfileRepository profileRepository;

    CreateInitialProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    // REQUIRES_NEW is required, not just tidiness: the only caller is an AFTER_COMMIT
    // @TransactionalEventListener, which runs before Spring unbinds the just-committed
    // transaction's EntityManager from the thread. Default (REQUIRED) propagation would silently
    // "participate" in that already-committed, about-to-be-discarded session instead of opening a
    // fresh one — the insert would be queued but never flushed/committed.
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Profile createEmpty(UserId userId) {
        return profileRepository.save(Profile.createEmpty(userId));
    }
}
