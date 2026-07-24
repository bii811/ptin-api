package com.example.ptin.taxdeclaration.infrastructure.mediation;

import com.example.ptin.shared.integration.infrastructure.client.ExternalApiCallLoggingInterceptorFactory;
import com.example.ptin.shared.mediation.TaxRisMediationProperties;
import com.example.ptin.taxdeclaration.domain.model.TaxDeclaration;
import com.example.ptin.taxdeclaration.domain.port.out.TaxMediationClient;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
class CrsPfmMediationRestAdapter implements TaxMediationClient {

    private static final String SND_CRS_PATH = "/mediate/TaxRIS/createPfmFromCRSDtl/SndCrs";
    private static final String SYSTEM_NAME = "TaxRIS-Mediation";

    private final RestClient restClient;
    private final TaxRisMediationProperties properties;

    CrsPfmMediationRestAdapter(RestClient.Builder restClientBuilder, TaxRisMediationProperties properties,
            ExternalApiCallLoggingInterceptorFactory loggingInterceptorFactory) {
        this.properties = properties;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout((int) properties.connectTimeoutMs());
        requestFactory.setReadTimeout((int) properties.readTimeoutMs());
        ClientHttpRequestFactory bufferingRequestFactory = new BufferingClientHttpRequestFactory(requestFactory);
        this.restClient = restClientBuilder
                .baseUrl(properties.baseUrl())
                .requestFactory(bufferingRequestFactory)
                .requestInterceptor(loggingInterceptorFactory.forSystem(SYSTEM_NAME))
                .build();
    }

    @Override
    public SubmissionResult submit(TaxDeclaration declaration) {
        SndCrsRequest request = SndCrsRequest.from(declaration, properties.hashKey());
        try {
            SndCrsResponse response = restClient.post()
                    .uri(SND_CRS_PATH)
                    .body(request)
                    .retrieve()
                    .body(SndCrsResponse.class);

            if (response == null) {
                return SubmissionResult.failure(null, "Empty response from mediation service");
            }
            if (!response.isSuccess()) {
                return SubmissionResult.failure(response.code(), response.message());
            }
            return SubmissionResult.success(response.code(), response.message());
        } catch (RestClientException e) {
            return SubmissionResult.failure(null, e.getMessage());
        }
    }
}
