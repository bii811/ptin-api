package com.example.ptin.ptin.application.usecase;

import com.example.ptin.ptin.domain.exception.PtinApplicationNotFoundException;
import com.example.ptin.ptin.domain.model.PtinApplication;
import com.example.ptin.ptin.domain.port.in.RejectPtinApplicationUseCase;
import com.example.ptin.ptin.domain.port.out.PtinApplicationRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class RejectPtinApplicationService implements RejectPtinApplicationUseCase {

    private final PtinApplicationRepository ptinApplicationRepository;

    RejectPtinApplicationService(PtinApplicationRepository ptinApplicationRepository) {
        this.ptinApplicationRepository = ptinApplicationRepository;
    }

    @Override
    @PreAuthorize("hasRole('AUTHORIZER')")
    public void reject(RejectPtinApplicationCommand command) {
        PtinApplication application = ptinApplicationRepository.findById(command.applicationId())
                .orElseThrow(() -> new PtinApplicationNotFoundException(command.applicationId()));
        application.reject(command.authorizerId(), command.reason());
        ptinApplicationRepository.save(application);
    }
}
