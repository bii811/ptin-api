package com.example.ptin.ptin.application.usecase;

import com.example.ptin.ptin.domain.model.PtinApplication;
import com.example.ptin.ptin.domain.model.PtinStatus;
import com.example.ptin.ptin.domain.port.in.ListPtinApplicationsByStatusUseCase;
import com.example.ptin.ptin.domain.port.out.PtinApplicationRepository;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class ListPtinApplicationsByStatusService implements ListPtinApplicationsByStatusUseCase {

    private final PtinApplicationRepository ptinApplicationRepository;

    ListPtinApplicationsByStatusService(PtinApplicationRepository ptinApplicationRepository) {
        this.ptinApplicationRepository = ptinApplicationRepository;
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<PtinApplication> list(PtinStatus status) {
        return status == null
                ? ptinApplicationRepository.findAll()
                : ptinApplicationRepository.findByStatus(status);
    }
}
