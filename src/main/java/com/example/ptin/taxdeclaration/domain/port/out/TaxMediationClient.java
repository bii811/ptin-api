package com.example.ptin.taxdeclaration.domain.port.out;

import com.example.ptin.taxdeclaration.domain.model.TaxDeclaration;

public interface TaxMediationClient {

    SubmissionResult submit(TaxDeclaration declaration);

    /** {@code outcomeUnknown}: no usable reply, so TaxRIS may still have recorded the declaration. */
    record SubmissionResult(boolean success, boolean outcomeUnknown, String resultCode, String resultMessage) {

        public static SubmissionResult success(String resultCode, String resultMessage) {
            return new SubmissionResult(true, false, resultCode, resultMessage);
        }

        public static SubmissionResult failure(String resultCode, String resultMessage) {
            return new SubmissionResult(false, false, resultCode, resultMessage);
        }

        public static SubmissionResult unknown(String message) {
            return new SubmissionResult(false, true, null, message);
        }
    }
}
