package com.example.ptin.ptin.infrastructure.mediation;

import com.example.ptin.ptin.domain.model.PtinApplication;
import com.example.ptin.ptin.domain.port.out.TinMediationClient;
import com.example.ptin.shared.integration.infrastructure.client.ExternalApiCallLoggingInterceptorFactory;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
class TinMediationRestAdapter implements TinMediationClient {

    private static final String REQ_TIN_INFO_PATH = "/mediate/TaxRIS/managePTinInformation/ReqTinInfo";
    private static final String SYSTEM_NAME = "TaxRIS-Mediation";

    private final RestClient restClient;
    private final TinMediationProperties properties;

    TinMediationRestAdapter(RestClient.Builder restClientBuilder, TinMediationProperties properties,
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
    public MediationResult submit(PtinApplication application) {
        ReqTinInfoRequest request = ReqTinInfoRequest.from(application, properties.hashKey(), properties.sys());
        try {
            ReqTinInfoResponse response = restClient.post()
                    .uri(REQ_TIN_INFO_PATH)
                    .body(request)
                    .retrieve()
                    .body(ReqTinInfoResponse.class);

            if (response == null) {
                return MediationResult.failure("Empty response from mediation service");
            }
            if (!response.isSuccess()) {
                return MediationResult.failure(response.errorMessage());
            }
            return MediationResult.success(response.tin(), response.taxrGvNm(), response.taxrFamNm());
        } catch (RestClientException e) {
            return MediationResult.failure(e.getMessage());
        }
    }
}
