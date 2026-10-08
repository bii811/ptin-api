package com.example.ptin.ptin.application.usecase;

import com.example.ptin.ptin.domain.exception.PtinApplicationNotFoundException;
import com.example.ptin.ptin.domain.model.PtinApplication;
import com.example.ptin.ptin.domain.port.in.UpdatePtinApplicationUseCase;
import com.example.ptin.ptin.domain.port.out.PtinApplicationRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class UpdatePtinApplicationService implements UpdatePtinApplicationUseCase {

    private final PtinApplicationRepository ptinApplicationRepository;

    UpdatePtinApplicationService(PtinApplicationRepository ptinApplicationRepository) {
        this.ptinApplicationRepository = ptinApplicationRepository;
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public PtinApplication update(UpdatePtinApplicationCommand command) {
        PtinApplication application = ptinApplicationRepository.findById(command.applicationId())
                .orElseThrow(() -> new PtinApplicationNotFoundException(command.applicationId()));
        application.edit(
                command.ptinType(),
                command.personalInfo(),
                command.contactInfo(),
                command.addressInfo(),
                command.employmentInfo(),
                command.incomeInfo(),
                command.financialInfo());
        return ptinApplicationRepository.save(application);
    }
}
