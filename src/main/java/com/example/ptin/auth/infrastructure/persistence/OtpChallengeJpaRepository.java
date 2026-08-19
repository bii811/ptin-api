package com.example.ptin.auth.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface OtpChallengeJpaRepository extends JpaRepository<OtpChallengeJpaEntity, UUID> {

    /**
     * SELECT ... FOR UPDATE, so two concurrent verification attempts cannot both read the same
     * attempt count and have the second overwrite the first's increment.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<OtpChallengeJpaEntity> findFirstByMobileNumberAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(
            String mobileNumber, String purpose);

    List<OtpChallengeJpaEntity> findByMobileNumberAndPurposeAndConsumedAtIsNull(String mobileNumber, String purpose);

    @Query("""
            select c.createdAt from OtpChallengeJpaEntity c
            where c.mobileNumber = :mobileNumber and c.purpose = :purpose and c.createdAt >= :since
            order by c.createdAt desc
            """)
    List<Instant> findIssueTimestampsSince(
            @Param("mobileNumber") String mobileNumber,
            @Param("purpose") String purpose,
            @Param("since") Instant since);
}
