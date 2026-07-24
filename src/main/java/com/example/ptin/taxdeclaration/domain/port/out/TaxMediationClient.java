package com.example.ptin.taxdeclaration.domain.port.out;

import com.example.ptin.taxdeclaration.domain.model.TaxDeclaration;

public interface TaxMediationClient {

    SubmissionResult submit(TaxDeclaration declaration);

    record SubmissionResult(boolean success, String resultCode, String resultMessage) {

        public static SubmissionResult success(String resultCode, String resultMessage) {
            return new SubmissionResult(true, resultCode, resultMessage);
        }

        public static SubmissionResult failure(String resultCode, String resultMessage) {
            return new SubmissionResult(false, resultCode, resultMessage);
        }
    }
}
