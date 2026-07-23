package com.example.ptin.ptin.domain.port.out;

import com.example.ptin.ptin.domain.model.PtinApplication;

public interface TinMediationClient {

    MediationResult submit(PtinApplication application);

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
