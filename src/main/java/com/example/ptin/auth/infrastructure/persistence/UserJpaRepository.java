package com.example.ptin.auth.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {

    Optional<UserJpaEntity> findByMobileNumber(String mobileNumber);

    /** SELECT ... FOR UPDATE, so concurrent password attempts serialise on the lockout counters. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UserJpaEntity> findWithLockById(UUID id);
}
