package com.example.ptin.profile.infrastructure.persistence;

import com.example.ptin.profile.domain.model.Profile;
import com.example.ptin.profile.domain.port.out.ProfileRepository;
import com.example.ptin.shared.identity.UserId;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
class ProfileJpaRepositoryAdapter implements ProfileRepository {

    private final ProfileJpaRepository jpaRepository;
    private final ProfilePersistenceMapper mapper;

    ProfileJpaRepositoryAdapter(ProfileJpaRepository jpaRepository, ProfilePersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Profile> findByUserId(UserId userId) {
        return jpaRepository.findByUserId(userId.value()).map(mapper::toDomain);
    }

    @Override
    public Profile save(Profile profile) {
        ProfileJpaEntity entity = jpaRepository.findById(profile.getId())
                .map(existing -> {
                    mapper.updateEntity(existing, profile);
                    return existing;
                })
                .orElseGet(() -> mapper.toEntity(profile));
        return mapper.toDomain(jpaRepository.save(entity));
    }
}
