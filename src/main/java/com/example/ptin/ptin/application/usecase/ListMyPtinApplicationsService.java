package com.example.ptin.ptin.application.usecase;

import com.example.ptin.ptin.domain.model.PtinApplication;
import com.example.ptin.ptin.domain.port.in.ListMyPtinApplicationsUseCase;
import com.example.ptin.ptin.domain.port.out.PtinApplicationRepository;
import com.example.ptin.shared.identity.UserId;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class ListMyPtinApplicationsService implements ListMyPtinApplicationsUseCase {

    private final PtinApplicationRepository ptinApplicationRepository;

    ListMyPtinApplicationsService(PtinApplicationRepository ptinApplicationRepository) {
        this.ptinApplicationRepository = ptinApplicationRepository;
    }

    @Override
    public List<PtinApplication> listMine(UserId userId) {
        return ptinApplicationRepository.findByUserId(userId);
    }
}
