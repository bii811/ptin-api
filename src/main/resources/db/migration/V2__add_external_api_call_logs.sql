CREATE TABLE external_api_call_logs (
    id              UUID PRIMARY KEY,
    system_name     VARCHAR(100) NOT NULL,
    endpoint        VARCHAR(500) NOT NULL,
    http_method     VARCHAR(10) NOT NULL,
    request_body    TEXT,
    response_body   TEXT,
    status_code     INT,
    success         BOOLEAN NOT NULL,
    error_message   VARCHAR(1000),
    duration_ms     BIGINT NOT NULL,
    called_at       TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_external_api_call_logs_system_name ON external_api_call_logs (system_name);
CREATE INDEX idx_external_api_call_logs_called_at ON external_api_call_logs (called_at);
