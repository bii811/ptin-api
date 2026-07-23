package com.example.ptin.ptin.domain.port.in;

import com.example.ptin.ptin.domain.model.PtinApplicationId;
import com.example.ptin.shared.identity.UserId;

public interface ApprovePtinApplicationUseCase {

    void approve(ApprovePtinApplicationCommand command);

    record ApprovePtinApplicationCommand(PtinApplicationId applicationId, UserId authorizerId) {
    }
}
