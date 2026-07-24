package com.example.ptin.taxdeclaration.infrastructure.mediation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// The "CrsPfms" echo object in the response is not needed - it mirrors the request fields (all
// null in observed responses) rather than confirming anything, so it's ignored rather than mapped.
@JsonIgnoreProperties(ignoreUnknown = true)
record SndCrsResponse(@JsonProperty("Result") Result result) {

    private static final String SUCCESS_CODE = "000";

    boolean isSuccess() {
        return result != null && SUCCESS_CODE.equals(result.cd());
    }

    String code() {
        return result == null ? null : result.cd();
    }

    String message() {
        return result == null ? null : result.msg();
    }

    record Result(@JsonProperty("CD") String cd, @JsonProperty("MSG") String msg, @JsonProperty("CNT") String cnt) {
    }
}
