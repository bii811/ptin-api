package com.example.ptin.auth.infrastructure.persistence;

import com.example.ptin.auth.domain.model.MobileNumber;
import com.example.ptin.auth.domain.model.OtpChallenge;
import com.example.ptin.auth.domain.model.OtpPurpose;
import com.example.ptin.auth.domain.port.out.OtpChallengeRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
class OtpChallengeJpaRepositoryAdapter implements OtpChallengeRepository {

    private final OtpChallengeJpaRepository jpaRepository;
    private final OtpChallengePersistenceMapper mapper;

    OtpChallengeJpaRepositoryAdapter(OtpChallengeJpaRepository jpaRepository, OtpChallengePersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<OtpChallenge> lockActiveChallenge(MobileNumber mobileNumber, OtpPurpose purpose) {
        return jpaRepository
                .findFirstByMobileNumberAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(
                        mobileNumber.value(), purpose.name())
                .map(mapper::toDomain);
    }

    @Override
    public List<OtpChallenge> findUnconsumed(MobileNumber mobileNumber, OtpPurpose purpose) {
        return jpaRepository
                .findByMobileNumberAndPurposeAndConsumedAtIsNull(mobileNumber.value(), purpose.name())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Instant> findIssueTimestampsSince(MobileNumber mobileNumber, OtpPurpose purpose, Instant since) {
        return jpaRepository.findIssueTimestampsSince(mobileNumber.value(), purpose.name(), since);
    }

    @Override
    public OtpChallenge save(OtpChallenge challenge) {
        OtpChallengeJpaEntity entity = jpaRepository.findById(challenge.getId().value())
                .map(existing -> {
                    mapper.updateEntity(existing, challenge);
                    return existing;
                })
                .orElseGet(() -> mapper.toEntity(challenge));
        return mapper.toDomain(jpaRepository.save(entity));
    }
}
