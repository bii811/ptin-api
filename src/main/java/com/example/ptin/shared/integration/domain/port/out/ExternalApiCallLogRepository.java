package com.example.ptin.shared.integration.domain.port.out;

import com.example.ptin.shared.integration.domain.model.ExternalApiCallLog;

public interface ExternalApiCallLogRepository {

    void save(ExternalApiCallLog log);
}
