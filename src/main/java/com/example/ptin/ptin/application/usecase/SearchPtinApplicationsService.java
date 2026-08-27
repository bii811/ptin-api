package com.example.ptin.ptin.application.usecase;

import com.example.ptin.ptin.domain.model.PtinApplication;
import com.example.ptin.ptin.domain.model.PtinApplicationSearchCriteria;
import com.example.ptin.ptin.domain.port.in.SearchPtinApplicationsUseCase;
import com.example.ptin.ptin.domain.port.out.PtinApplicationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class SearchPtinApplicationsService implements SearchPtinApplicationsUseCase {

    private final PtinApplicationRepository ptinApplicationRepository;

    SearchPtinApplicationsService(PtinApplicationRepository ptinApplicationRepository) {
        this.ptinApplicationRepository = ptinApplicationRepository;
    }

    @Override
    @PreAuthorize("hasRole('AUTHORIZER')")
    public Page<PtinApplication> search(PtinApplicationSearchCriteria criteria, Pageable pageable) {
        return ptinApplicationRepository.search(criteria, pageable);
    }
}
