package com.example.ptin.ptin.domain.port.out;

import com.example.ptin.ptin.domain.model.PtinApplication;
import com.example.ptin.shared.identity.UserId;

public interface TinMediationClient {

    /** {@code functionSource}/{@code triggeredBy} identify the caller in the external-call audit log. */
    MediationResult submit(PtinApplication application, String functionSource, UserId triggeredBy);

    record MediationResult(
            boolean success, String tin, String confirmedGivenName, String confirmedFamilyName, String errorMessage) {

        public static MediationResult success(String tin, String confirmedGivenName, String confirmedFamilyName) {
            return new MediationResult(true, tin, confirmedGivenName, confirmedFamilyName, null);
        }

        public static MediationResult failure(String errorMessage) {
            return new MediationResult(false, null, null, null, errorMessage);
        }
    }
}
