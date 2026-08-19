package com.example.ptin.ptin.application.usecase;

import com.example.ptin.ptin.domain.model.PtinApplication;
import com.example.ptin.ptin.domain.model.PtinStatus;
import com.example.ptin.ptin.domain.model.TaxpayerIdentificationNumber;
import com.example.ptin.ptin.domain.port.in.FindIssuedTinUseCase;
import com.example.ptin.ptin.domain.port.out.PtinApplicationRepository;
import com.example.ptin.shared.identity.UserId;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class FindIssuedTinService implements FindIssuedTinUseCase {

    private final PtinApplicationRepository ptinApplicationRepository;

    FindIssuedTinService(PtinApplicationRepository ptinApplicationRepository) {
        this.ptinApplicationRepository = ptinApplicationRepository;
    }

    @Override
    public Optional<String> findIssuedTinByUser(UserId userId) {
        return ptinApplicationRepository.findByUserId(userId).stream()
                .filter(application -> application.getStatus() == PtinStatus.ISSUED)
                .map(PtinApplication::getTin)
                .filter(Objects::nonNull)
                .map(TaxpayerIdentificationNumber::value)
                .findFirst();
    }

    @Override
    public Optional<UserId> findUserIdByIssuedTin(String tin) {
        if (tin == null || tin.isBlank()) {
            return Optional.empty();
        }
        return ptinApplicationRepository.findIssuedByTin(tin).map(PtinApplication::getUserId);
    }
}
