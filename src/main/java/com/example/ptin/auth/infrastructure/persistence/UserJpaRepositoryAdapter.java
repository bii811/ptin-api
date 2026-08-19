package com.example.ptin.auth.infrastructure.persistence;

import com.example.ptin.auth.domain.model.MobileNumber;
import com.example.ptin.auth.domain.model.User;
import com.example.ptin.auth.domain.port.out.UserRepository;
import com.example.ptin.shared.identity.UserId;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
class UserJpaRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;
    private final UserPersistenceMapper mapper;

    UserJpaRepositoryAdapter(UserJpaRepository jpaRepository, UserPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<User> findByMobileNumber(MobileNumber mobileNumber) {
        return jpaRepository.findByMobileNumber(mobileNumber.value()).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findById(UserId id) {
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public Optional<User> lockById(UserId id) {
        return jpaRepository.findWithLockById(id.value()).map(mapper::toDomain);
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = jpaRepository.findById(user.getId().value())
                .map(existing -> {
                    mapper.updateEntity(existing, user);
                    return existing;
                })
                .orElseGet(() -> mapper.toEntity(user));
        return mapper.toDomain(jpaRepository.save(entity));
    }
}
