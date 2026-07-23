package com.example.ptin.ptin.application.usecase;

import com.example.ptin.ptin.domain.model.PtinApplication;
import com.example.ptin.ptin.domain.model.PtinStatus;
import com.example.ptin.ptin.domain.port.in.ListPendingPtinApplicationsUseCase;
import com.example.ptin.ptin.domain.port.out.PtinApplicationRepository;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class ListPendingPtinApplicationsService implements ListPendingPtinApplicationsUseCase {

    private final PtinApplicationRepository ptinApplicationRepository;

    ListPendingPtinApplicationsService(PtinApplicationRepository ptinApplicationRepository) {
        this.ptinApplicationRepository = ptinApplicationRepository;
    }

    @Override
    @PreAuthorize("hasRole('AUTHORIZER')")
    public List<PtinApplication> listPending() {
        return ptinApplicationRepository.findByStatus(PtinStatus.PENDING_APPROVAL);
    }
}
