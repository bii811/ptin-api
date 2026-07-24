package com.example.ptin.shared.integration.infrastructure.client;

import com.example.ptin.shared.integration.domain.port.out.ExternalApiCallLogRepository;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.stereotype.Component;

/**
 * Lets any outbound REST adapter opt into call logging by adding
 * {@code forSystem("...")} as a {@code RestClient.Builder} request interceptor, without each
 * adapter wiring its own persistence.
 */
@Component
public class ExternalApiCallLoggingInterceptorFactory {

    private final ExternalApiCallLogRepository logRepository;

    public ExternalApiCallLoggingInterceptorFactory(ExternalApiCallLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public ClientHttpRequestInterceptor forSystem(String systemName) {
        return new ExternalApiCallLoggingInterceptor(systemName, logRepository);
    }
}
