DROP TABLE external_api_call_logs;

CREATE TABLE taxris_api_call_logs (
    id               UUID PRIMARY KEY,
    function_source  VARCHAR(100) NOT NULL,
    reference_id     VARCHAR(64),
    triggered_by     VARCHAR(64),
    endpoint_url     VARCHAR(500) NOT NULL,
    request_headers  TEXT,
    request_body     TEXT,
    response_body    TEXT,
    result_code      VARCHAR(10),
    result_message   VARCHAR(1000),
    attempt_no       INT NOT NULL,
    called_at        TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_taxris_api_call_logs_reference ON taxris_api_call_logs (reference_id);
CREATE INDEX idx_taxris_api_call_logs_function_called ON taxris_api_call_logs (function_source, called_at);

ALTER TABLE ptins ADD COLUMN ptin_type VARCHAR(20) NOT NULL DEFAULT 'INDIVIDUAL';
-- Pre-existing applications with a labor id were issued through managePTinInformation (LMIS).
UPDATE ptins SET ptin_type = 'LABOR' WHERE labo_id IS NOT NULL AND labo_id <> '';
