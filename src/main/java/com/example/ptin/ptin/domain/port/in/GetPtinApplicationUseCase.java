package com.example.ptin.ptin.domain.port.in;

import com.example.ptin.ptin.domain.model.PtinApplication;
import com.example.ptin.ptin.domain.model.PtinApplicationId;
import com.example.ptin.shared.identity.UserId;

public interface GetPtinApplicationUseCase {

    /**
     * @param requesterIsAuthorizer whether the caller holds the AUTHORIZER role; authorizers may view
     *                              any application, applicants may only view their own.
     */
    PtinApplication getById(PtinApplicationId id, UserId requestingUserId, boolean requesterIsAuthorizer);
}
