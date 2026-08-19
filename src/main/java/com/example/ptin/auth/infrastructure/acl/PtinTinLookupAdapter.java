package com.example.ptin.auth.infrastructure.acl;

import com.example.ptin.auth.domain.port.out.TaxpayerTinLookupPort;
import com.example.ptin.ptin.domain.port.in.FindIssuedTinUseCase;
import com.example.ptin.shared.identity.UserId;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
class PtinTinLookupAdapter implements TaxpayerTinLookupPort {

    private final FindIssuedTinUseCase findIssuedTinUseCase;

    PtinTinLookupAdapter(FindIssuedTinUseCase findIssuedTinUseCase) {
        this.findIssuedTinUseCase = findIssuedTinUseCase;
    }

    @Override
    public Optional<String> findIssuedTin(UserId userId) {
        return findIssuedTinUseCase.findIssuedTinByUser(userId);
    }

    @Override
    public Optional<UserId> findUserIdByIssuedTin(String tin) {
        return findIssuedTinUseCase.findUserIdByIssuedTin(tin);
    }
}
