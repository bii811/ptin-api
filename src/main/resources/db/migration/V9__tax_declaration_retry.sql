-- Retry support for tax declarations: attempt counter + start of the latest attempt, and one live declaration
-- per invoice (FAILED ones are excluded so a rejected invoice can be corrected and filed again).
ALTER TABLE tax_declarations
    ADD COLUMN retry_count     INT         NOT NULL DEFAULT 0,
    ADD COLUMN last_attempt_at TIMESTAMPTZ;

UPDATE tax_declarations SET last_attempt_at = submitted_at;
ALTER TABLE tax_declarations ALTER COLUMN last_attempt_at SET NOT NULL;

CREATE UNIQUE INDEX uq_tax_declarations_live_invoice
    ON tax_declarations (tin, invoice_number)
    WHERE status <> 'FAILED' AND deleted_at IS NULL;
