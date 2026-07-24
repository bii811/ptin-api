package com.example.ptin.taxdeclaration.infrastructure.acl;

import com.example.ptin.ptin.domain.model.PtinStatus;
import com.example.ptin.ptin.domain.port.in.ListMyPtinApplicationsUseCase;
import com.example.ptin.shared.identity.UserId;
import com.example.ptin.taxdeclaration.domain.port.out.TaxpayerTinPort;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
class TaxpayerTinAdapter implements TaxpayerTinPort {

    private final ListMyPtinApplicationsUseCase listMyPtinApplicationsUseCase;

    TaxpayerTinAdapter(ListMyPtinApplicationsUseCase listMyPtinApplicationsUseCase) {
        this.listMyPtinApplicationsUseCase = listMyPtinApplicationsUseCase;
    }

    @Override
    public Optional<String> findIssuedTin(UserId userId) {
        return listMyPtinApplicationsUseCase.listMine(userId).stream()
                .filter(application -> application.getStatus() == PtinStatus.ISSUED)
                .findFirst()
                .map(application -> application.getTin().value());
    }
}
