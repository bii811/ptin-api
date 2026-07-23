package com.example.ptin.ptin.application.usecase;

import com.example.ptin.ptin.domain.exception.PtinApplicationNotFoundException;
import com.example.ptin.ptin.domain.model.PtinApplication;
import com.example.ptin.ptin.domain.model.PtinApplicationId;
import com.example.ptin.ptin.domain.port.in.GetPtinApplicationUseCase;
import com.example.ptin.ptin.domain.port.out.PtinApplicationRepository;
import com.example.ptin.shared.identity.UserId;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class GetPtinApplicationService implements GetPtinApplicationUseCase {

    private final PtinApplicationRepository ptinApplicationRepository;

    GetPtinApplicationService(PtinApplicationRepository ptinApplicationRepository) {
        this.ptinApplicationRepository = ptinApplicationRepository;
    }

    @Override
    public PtinApplication getById(PtinApplicationId id, UserId requestingUserId, boolean requesterIsAuthorizer) {
        PtinApplication application =
                ptinApplicationRepository.findById(id).orElseThrow(() -> new PtinApplicationNotFoundException(id));
        if (!requesterIsAuthorizer && !application.getUserId().equals(requestingUserId)) {
            throw new AccessDeniedException("Not permitted to view this application");
        }
        return application;
    }
}
