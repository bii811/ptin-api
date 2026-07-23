package com.example.ptin.ptin.application.usecase;

import com.example.ptin.ptin.domain.exception.PtinApplicationNotFoundException;
import com.example.ptin.ptin.domain.model.PtinApplication;
import com.example.ptin.ptin.domain.model.PtinApplicationId;
import com.example.ptin.ptin.domain.port.out.PtinApplicationRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * A separate bean (not just separate methods) so {@link PtinMediationCoordinator} calls these
 * through a real Spring proxy rather than self-invoking — {@code @Transactional} is silently
 * ignored on self-invoked calls within the same class.
 *
 * REQUIRES_NEW matters here specifically because {@link PtinMediationCoordinator} can be invoked
 * from an AFTER_COMMIT {@code @TransactionalEventListener} (the post-approval mediation flow),
 * which runs before Spring unbinds the just-committed transaction's EntityManager from the thread.
 * Default (REQUIRED) propagation would silently "participate" in that already-committed,
 * about-to-be-discarded session instead of opening a fresh one, and the write would be queued but
 * never flushed/committed.
 */
@Component
class PtinApplicationTransactionalGateway {

    private final PtinApplicationRepository ptinApplicationRepository;

    PtinApplicationTransactionalGateway(PtinApplicationRepository ptinApplicationRepository) {
        this.ptinApplicationRepository = ptinApplicationRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    PtinApplication loadOrThrow(PtinApplicationId id) {
        return ptinApplicationRepository.findById(id).orElseThrow(() -> new PtinApplicationNotFoundException(id));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void save(PtinApplication application) {
        ptinApplicationRepository.save(application);
    }
}
