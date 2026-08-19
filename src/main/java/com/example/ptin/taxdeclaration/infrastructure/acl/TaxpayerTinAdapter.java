package com.example.ptin.taxdeclaration.infrastructure.acl;

import com.example.ptin.ptin.domain.port.in.FindIssuedTinUseCase;
import com.example.ptin.shared.identity.UserId;
import com.example.ptin.taxdeclaration.domain.port.out.TaxpayerTinPort;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
class TaxpayerTinAdapter implements TaxpayerTinPort {

    private final FindIssuedTinUseCase findIssuedTinUseCase;

    TaxpayerTinAdapter(FindIssuedTinUseCase findIssuedTinUseCase) {
        this.findIssuedTinUseCase = findIssuedTinUseCase;
    }

    @Override
    public Optional<String> findIssuedTin(UserId userId) {
        return findIssuedTinUseCase.findIssuedTinByUser(userId);
    }
}
