package com.example.ptin.shared.integration.infrastructure.client;

import com.example.ptin.shared.integration.domain.model.ExternalApiCallLog;
import com.example.ptin.shared.integration.domain.port.out.ExternalApiCallLogRepository;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

/**
 * Requires the owning {@link org.springframework.web.client.RestClient} to be built with a
 * {@link org.springframework.http.client.BufferingClientHttpRequestFactory}, otherwise reading the
 * response body here would consume it before the caller's message converters can.
 */
class ExternalApiCallLoggingInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(ExternalApiCallLoggingInterceptor.class);

    private final String systemName;
    private final ExternalApiCallLogRepository logRepository;

    ExternalApiCallLoggingInterceptor(String systemName, ExternalApiCallLogRepository logRepository) {
        this.systemName = systemName;
        this.logRepository = logRepository;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        Instant calledAt = Instant.now();
        String requestBody = new String(body, StandardCharsets.UTF_8);
        try {
            ClientHttpResponse response = execution.execute(request, body);
            String responseBody = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
            record(request, requestBody, responseBody, response.getStatusCode().value(),
                    response.getStatusCode().is2xxSuccessful(), null, calledAt);
            return response;
        } catch (IOException e) {
            record(request, requestBody, null, null, false, e.getMessage(), calledAt);
            throw e;
        }
    }

    private void record(HttpRequest request, String requestBody, String responseBody, Integer statusCode,
            boolean success, String errorMessage, Instant calledAt) {
        try {
            long durationMs = Instant.now().toEpochMilli() - calledAt.toEpochMilli();
            logRepository.save(ExternalApiCallLog.of(systemName, request.getURI().toString(),
                    request.getMethod().name(), requestBody, responseBody, statusCode, success, errorMessage,
                    durationMs, calledAt));
        } catch (RuntimeException e) {
            log.warn("Failed to persist external API call log for system '{}'", systemName, e);
        }
    }
}
