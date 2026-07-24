package com.example.ptin.taxdeclaration.infrastructure.mediation;

import com.example.ptin.taxdeclaration.domain.model.TaxDeclaration;
import com.fasterxml.jackson.annotation.JsonProperty;

record SndCrsRequest(@JsonProperty("HASH_KEY") String hashKey, @JsonProperty("CrsPfms") CrsPfmsPayload crsPfms) {

    static SndCrsRequest from(TaxDeclaration declaration, String hashKey) {
        return new SndCrsRequest(hashKey, CrsPfmsPayload.from(declaration));
    }
}
