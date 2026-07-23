package com.example.ptin.ptin.infrastructure.mediation;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ReqTinInfoResponse(
        @JsonProperty("RESULT_CD") String resultCode,
        @JsonProperty("RESULT_MSG") String resultMessage,
        @JsonProperty("TIN") String tin,
        @JsonProperty("TAXR_GV_NM") String taxrGvNm,
        @JsonProperty("TAXR_FAM_NM") String taxrFamNm) {

    // Assumption: the mediation service is treated as successful when it returns a non-blank TIN.
    // Adjust once the external API's actual result-code contract is documented.
    boolean isSuccess() {
        return tin != null && !tin.isBlank();
    }

    String errorMessage() {
        return resultMessage != null && !resultMessage.isBlank() ? resultMessage : "Mediation service did not return a TIN";
    }
}
