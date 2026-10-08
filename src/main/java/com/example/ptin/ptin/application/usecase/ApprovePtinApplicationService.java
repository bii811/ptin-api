package com.example.ptin.ptin.application.usecase;

import com.example.ptin.ptin.domain.event.PtinApplicationApprovedEvent;
import com.example.ptin.ptin.domain.exception.PtinApplicationNotFoundException;
import com.example.ptin.ptin.domain.model.PtinApplication;
import com.example.ptin.ptin.domain.port.in.ApprovePtinApplicationUseCase;
import com.example.ptin.ptin.domain.port.out.PtinApplicationRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class ApprovePtinApplicationService implements ApprovePtinApplicationUseCase {

    private final PtinApplicationRepository ptinApplicationRepository;
    private final ApplicationEventPublisher eventPublisher;

    ApprovePtinApplicationService(
            PtinApplicationRepository ptinApplicationRepository, ApplicationEventPublisher eventPublisher) {
        this.ptinApplicationRepository = ptinApplicationRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @PreAuthorize("hasRole('AUTHORIZER')")
    public void approve(ApprovePtinApplicationCommand command) {
        PtinApplication application = ptinApplicationRepository.findById(command.applicationId())
                .orElseThrow(() -> new PtinApplicationNotFoundException(command.applicationId()));
        application.approve(command.authorizerId());
        ptinApplicationRepository.save(application);

        // Published for AFTER_COMMIT delivery (see PtinApplicationApprovedEventListener) so the
        // external mediation HTTP call never happens while this transaction still holds a DB connection.
        eventPublisher.publishEvent(new PtinApplicationApprovedEvent(application.getId(), command.authorizerId()));
    }
}
