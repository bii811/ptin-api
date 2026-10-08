package com.example.ptin.taxdeclaration.infrastructure.mediation;

import com.example.ptin.shared.mediation.CallContext;
import com.example.ptin.shared.mediation.TaxRisClient;
import com.example.ptin.shared.mediation.TaxRisClient.Exchange;
import com.example.ptin.shared.mediation.TaxRisMediationProperties;
import com.example.ptin.taxdeclaration.domain.model.TaxDeclaration;
import com.example.ptin.taxdeclaration.domain.port.out.TaxMediationClient;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
class CrsPfmMediationRestAdapter implements TaxMediationClient {

    private static final String SND_CRS_PATH = "/mediate/TaxRIS/createPfmFromCRSDtl/SndCrs";
    private static final String FUNCTION_SOURCE = "TAX_DECLARATION_SUBMIT";

    private final TaxRisClient taxRisClient;
    private final TaxRisMediationProperties properties;
    private final ObjectMapper objectMapper;

    CrsPfmMediationRestAdapter(
            TaxRisClient taxRisClient, TaxRisMediationProperties properties, ObjectMapper objectMapper) {
        this.taxRisClient = taxRisClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public SubmissionResult submit(TaxDeclaration declaration) {
        SndCrsRequest request = SndCrsRequest.from(declaration, properties.hashKey());
        CallContext ctx = new CallContext(
                FUNCTION_SOURCE, declaration.getId().toString(), declaration.getUserId().toString(), 1);
        Exchange exchange = taxRisClient.post(SND_CRS_PATH, request, ctx);

        if (exchange.status() == null) {
            return SubmissionResult.failure(null, exchange.error());
        }
        SndCrsResponse response;
        try {
            response = objectMapper.readValue(exchange.body(), SndCrsResponse.class);
        } catch (RuntimeException e) {
            return SubmissionResult.failure(null, "Unparseable response from mediation service (HTTP " + exchange.status() + ")");
        }
        if (response == null) {
            return SubmissionResult.failure(null, "Empty response from mediation service");
        }
        if (!response.isSuccess()) {
            return SubmissionResult.failure(response.code(), response.message());
        }
        return SubmissionResult.success(response.code(), response.message());
    }
}
