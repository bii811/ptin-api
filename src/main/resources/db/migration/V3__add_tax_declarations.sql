CREATE TABLE tax_declarations (
    id                  UUID PRIMARY KEY,
    user_id             UUID NOT NULL,
    status              VARCHAR(20) NOT NULL,

    -- CrsPfms external form fields
    tin                 VARCHAR(12) NOT NULL,
    invoice_number      VARCHAR(50) NOT NULL,
    invoice_date        VARCHAR(8) NOT NULL,
    buyer_tin           VARCHAR(20),
    buyer_full_name     VARCHAR(200),
    sale_count          VARCHAR(10) NOT NULL,
    supply_amount       VARCHAR(28) NOT NULL,
    service_fee         VARCHAR(28),
    excise_amount       VARCHAR(28),
    vat_amount          VARCHAR(28),
    sale_amount         VARCHAR(28) NOT NULL,
    discount_amount     VARCHAR(28),
    sale_cancel_count   VARCHAR(10),
    sale_cancel_amount  VARCHAR(28),
    items_json          TEXT NOT NULL,

    -- mediation outcome
    submitted_at        TIMESTAMPTZ NOT NULL,
    result_code         VARCHAR(10),
    result_message      VARCHAR(1000),

    created_at          TIMESTAMPTZ NOT NULL,
    created_by          VARCHAR(64),
    updated_at          TIMESTAMPTZ NOT NULL,
    updated_by          VARCHAR(64),
    deleted_at          TIMESTAMPTZ,
    deleted_by          VARCHAR(64),
    CONSTRAINT fk_tax_declarations_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_tax_declarations_user_id ON tax_declarations (user_id);
