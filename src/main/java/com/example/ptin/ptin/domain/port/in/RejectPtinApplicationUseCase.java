package com.example.ptin.ptin.domain.port.in;

import com.example.ptin.ptin.domain.model.PtinApplicationId;
import com.example.ptin.shared.identity.UserId;

public interface RejectPtinApplicationUseCase {

    void reject(RejectPtinApplicationCommand command);

    record RejectPtinApplicationCommand(PtinApplicationId applicationId, UserId authorizerId, String reason) {
    }
}
