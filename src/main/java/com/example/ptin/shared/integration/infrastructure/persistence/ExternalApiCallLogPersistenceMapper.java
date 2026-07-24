package com.example.ptin.shared.integration.infrastructure.persistence;

import com.example.ptin.shared.integration.domain.model.ExternalApiCallLog;
import org.springframework.stereotype.Component;

@Component
class ExternalApiCallLogPersistenceMapper {

    ExternalApiCallLogJpaEntity toEntity(ExternalApiCallLog log) {
        return new ExternalApiCallLogJpaEntity(
                log.getId(),
                log.getSystemName(),
                log.getEndpoint(),
                log.getHttpMethod(),
                log.getRequestBody(),
                log.getResponseBody(),
                log.getStatusCode(),
                log.isSuccess(),
                log.getErrorMessage(),
                log.getDurationMs(),
                log.getCalledAt());
    }
}
