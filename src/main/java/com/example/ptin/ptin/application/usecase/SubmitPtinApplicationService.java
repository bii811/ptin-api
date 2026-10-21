package com.example.ptin.ptin.application.usecase;

import com.example.ptin.ptin.domain.exception.ProfileIncompleteException;
import com.example.ptin.ptin.domain.exception.PtinApplicationAlreadyExistsException;
import com.example.ptin.ptin.domain.model.PtinApplication;
import com.example.ptin.ptin.domain.model.PtinApplicationId;
import com.example.ptin.ptin.domain.port.in.SubmitPtinApplicationUseCase;
import com.example.ptin.ptin.domain.port.out.PtinApplicationRepository;
import com.example.ptin.ptin.domain.port.out.ProfileCompletionPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class SubmitPtinApplicationService implements SubmitPtinApplicationUseCase {

    private final PtinApplicationRepository ptinApplicationRepository;
    private final ProfileCompletionPort profileCompletionPort;

    SubmitPtinApplicationService(
            PtinApplicationRepository ptinApplicationRepository, ProfileCompletionPort profileCompletionPort) {
        this.ptinApplicationRepository = ptinApplicationRepository;
        this.profileCompletionPort = profileCompletionPort;
    }

    @Override
    public PtinApplicationId submit(SubmitPtinApplicationCommand command) {
        if (!profileCompletionPort.isProfileComplete(command.userId())) {
            throw new ProfileIncompleteException();
        }
        // one application per user, whatever its status (pending, rejected, failed and issued all block a new one)
        if (!ptinApplicationRepository.findByUserId(command.userId()).isEmpty()) {
            throw new PtinApplicationAlreadyExistsException();
        }
        PtinApplication application = PtinApplication.submit(
                command.userId(),
                command.ptinType(),
                command.personalInfo(),
                command.contactInfo(),
                command.addressInfo(),
                command.employmentInfo(),
                command.incomeInfo(),
                command.financialInfo());
        return ptinApplicationRepository.save(application).getId();
    }
}
