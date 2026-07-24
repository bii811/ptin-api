package com.example.ptin.shared.integration.infrastructure.persistence;

import com.example.ptin.shared.integration.domain.model.ExternalApiCallLog;
import com.example.ptin.shared.integration.domain.port.out.ExternalApiCallLogRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
class ExternalApiCallLogJpaRepositoryAdapter implements ExternalApiCallLogRepository {

    private final ExternalApiCallLogJpaRepository jpaRepository;
    private final ExternalApiCallLogPersistenceMapper mapper;

    ExternalApiCallLogJpaRepositoryAdapter(ExternalApiCallLogJpaRepository jpaRepository,
            ExternalApiCallLogPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    /**
     * REQUIRES_NEW so the call log survives even when the caller's own transaction
     * (e.g. the use case that triggered the external call) later rolls back.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(ExternalApiCallLog log) {
        jpaRepository.save(mapper.toEntity(log));
    }
}
