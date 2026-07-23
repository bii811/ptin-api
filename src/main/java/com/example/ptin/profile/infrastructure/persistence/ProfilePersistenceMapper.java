package com.example.ptin.profile.infrastructure.persistence;

import com.example.ptin.profile.domain.model.Profile;
import com.example.ptin.shared.identity.UserId;
import org.springframework.stereotype.Component;

@Component
class ProfilePersistenceMapper {

    ProfileJpaEntity toEntity(Profile profile) {
        return new ProfileJpaEntity(
                profile.getId(),
                profile.getUserId().value(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getAvatarUrl());
    }

    void updateEntity(ProfileJpaEntity entity, Profile profile) {
        entity.setFirstName(profile.getFirstName());
        entity.setLastName(profile.getLastName());
        entity.setAvatarUrl(profile.getAvatarUrl());
    }

    Profile toDomain(ProfileJpaEntity entity) {
        return Profile.reconstitute(
                entity.getId(),
                new UserId(entity.getUserId()),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getAvatarUrl());
    }
}
