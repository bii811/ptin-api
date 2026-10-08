package com.example.ptin.shared.mediation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

interface TaxRisApiCallLogJpaRepository extends JpaRepository<TaxRisApiCallLog, java.util.UUID> {
}

/**
 * REQUIRES_NEW so the log survives a rollback of the caller's transaction, and failures to log
 * never break the business call.
 */
@Component
class TaxRisApiCallLogWriter {

    private static final Logger log = LoggerFactory.getLogger(TaxRisApiCallLogWriter.class);

    private final TaxRisApiCallLogJpaRepository repository;

    TaxRisApiCallLogWriter(TaxRisApiCallLogJpaRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void write(TaxRisApiCallLog entry) {
        try {
            repository.save(entry);
        } catch (RuntimeException e) {
            log.warn("Failed to persist TaxRIS API call log for {}", entry.getFunctionSource(), e);
        }
    }
}
