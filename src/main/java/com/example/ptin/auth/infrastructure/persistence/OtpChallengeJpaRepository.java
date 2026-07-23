package com.example.ptin.auth.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface OtpChallengeJpaRepository extends JpaRepository<OtpChallengeJpaEntity, UUID> {

    Optional<OtpChallengeJpaEntity> findFirstByMobileNumberAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(
            String mobileNumber, String purpose);
}
