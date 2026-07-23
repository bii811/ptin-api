package com.example.ptin.ptin.domain.port.in;

import com.example.ptin.ptin.domain.model.PtinApplicationId;
import com.example.ptin.shared.identity.UserId;

public interface RetryPtinIssuanceUseCase {

    void retry(RetryPtinIssuanceCommand command);

    record RetryPtinIssuanceCommand(PtinApplicationId applicationId, UserId authorizerId) {
    }
}
