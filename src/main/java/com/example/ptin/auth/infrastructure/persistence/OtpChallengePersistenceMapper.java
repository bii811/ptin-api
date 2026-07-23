package com.example.ptin.auth.infrastructure.persistence;

import com.example.ptin.auth.domain.model.MobileNumber;
import com.example.ptin.auth.domain.model.OtpChallenge;
import com.example.ptin.auth.domain.model.OtpChallengeId;
import com.example.ptin.auth.domain.model.OtpPurpose;
import org.springframework.stereotype.Component;

@Component
class OtpChallengePersistenceMapper {

    OtpChallengeJpaEntity toEntity(OtpChallenge challenge) {
        return new OtpChallengeJpaEntity(
                challenge.getId().value(),
                challenge.getMobileNumber().value(),
                challenge.getPurpose().name(),
                challenge.getHashedCode(),
                challenge.getExpiresAt(),
                challenge.getAttemptCount(),
                challenge.getMaxAttempts(),
                challenge.getConsumedAt());
    }

    void updateEntity(OtpChallengeJpaEntity entity, OtpChallenge challenge) {
        entity.setAttemptCount(challenge.getAttemptCount());
        entity.setConsumedAt(challenge.getConsumedAt());
    }

    OtpChallenge toDomain(OtpChallengeJpaEntity entity) {
        return OtpChallenge.reconstitute(
                new OtpChallengeId(entity.getId()),
                new MobileNumber(entity.getMobileNumber()),
                OtpPurpose.valueOf(entity.getPurpose()),
                entity.getCodeHash(),
                entity.getExpiresAt(),
                entity.getMaxAttempts(),
                entity.getAttemptCount(),
                entity.getConsumedAt());
    }
}
