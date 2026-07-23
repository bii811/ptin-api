CREATE TABLE users (
    id              UUID PRIMARY KEY,
    mobile_number   VARCHAR(20) NOT NULL,
    role            VARCHAR(20) NOT NULL,
    status          VARCHAR(20) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL,
    created_by      VARCHAR(64),
    updated_at      TIMESTAMPTZ NOT NULL,
    updated_by      VARCHAR(64),
    deleted_at      TIMESTAMPTZ,
    deleted_by      VARCHAR(64),
    CONSTRAINT uq_users_mobile_number UNIQUE (mobile_number)
);

CREATE TABLE otp_challenges (
    id              UUID PRIMARY KEY,
    mobile_number   VARCHAR(20) NOT NULL,
    purpose         VARCHAR(20) NOT NULL,
    code_hash       VARCHAR(255) NOT NULL,
    expires_at      TIMESTAMPTZ NOT NULL,
    attempt_count   INT NOT NULL DEFAULT 0,
    max_attempts    INT NOT NULL DEFAULT 5,
    consumed_at     TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL,
    created_by      VARCHAR(64),
    updated_at      TIMESTAMPTZ NOT NULL,
    updated_by      VARCHAR(64),
    deleted_at      TIMESTAMPTZ,
    deleted_by      VARCHAR(64)
);

CREATE INDEX idx_otp_challenges_lookup ON otp_challenges (mobile_number, purpose, consumed_at);

CREATE TABLE profiles (
    id              UUID PRIMARY KEY,
    user_id         UUID NOT NULL,
    first_name      VARCHAR(100),
    last_name       VARCHAR(100),
    avatar_url      VARCHAR(500),
    created_at      TIMESTAMPTZ NOT NULL,
    created_by      VARCHAR(64),
    updated_at      TIMESTAMPTZ NOT NULL,
    updated_by      VARCHAR(64),
    deleted_at      TIMESTAMPTZ,
    deleted_by      VARCHAR(64),
    CONSTRAINT uq_profiles_user_id UNIQUE (user_id),
    CONSTRAINT fk_profiles_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE ptins (
    id                  UUID PRIMARY KEY,
    user_id             UUID NOT NULL,
    status              VARCHAR(30) NOT NULL,

    -- workflow / audit-of-business-events fields
    submitted_at        TIMESTAMPTZ NOT NULL,
    approved_by         UUID,
    approved_at         TIMESTAMPTZ,
    rejected_by         UUID,
    rejected_at         TIMESTAMPTZ,
    rejection_reason    VARCHAR(500),
    issued_at           TIMESTAMPTZ,
    failure_reason      VARCHAR(1000),
    failed_at           TIMESTAMPTZ,
    retry_count         INT NOT NULL DEFAULT 0,

    -- ReqTinInfo external form fields
    labo_id             VARCHAR(20),
    taxr_gv_nm          VARCHAR(200),
    taxr_fam_nm         VARCHAR(200),
    gnd_tp              VARCHAR(1),
    nat_tp              VARCHAR(2),
    bday                VARCHAR(8),
    tel_no              VARCHAR(20),
    hp_no               VARCHAR(20),
    fax_no              VARCHAR(20),
    email               VARCHAR(200),
    ind_id              VARCHAR(20),
    ind_id_tp           VARCHAR(2),
    famb_issu_plc       VARCHAR(300),
    addr_seqno          VARCHAR(8),
    unit_no             VARCHAR(3),
    road_nm             VARCHAR(200),
    hou_no              VARCHAR(200),
    pbox_no             VARCHAR(10),
    pub_offi_yn         VARCHAR(1),
    ind_busn_opr_yn     VARCHAR(1),
    pvt_co_emp_yn       VARCHAR(1),
    etc_job_cont        TEXT,
    divd_inc_yn         VARCHAR(1),
    rent_inc_yn         VARCHAR(1),
    etc_inc_cont        TEXT,
    srl_amt             VARCHAR(28),
    work_tin            VARCHAR(12),
    work_addr_seqno     VARCHAR(8),
    work_unit_no        VARCHAR(3),
    work_road_nm        VARCHAR(200),
    work_hou_no         VARCHAR(200),
    bank_acc_no         VARCHAR(28),
    so_se_no            VARCHAR(20),
    tin                 VARCHAR(12),

    created_at          TIMESTAMPTZ NOT NULL,
    created_by          VARCHAR(64),
    updated_at          TIMESTAMPTZ NOT NULL,
    updated_by          VARCHAR(64),
    deleted_at          TIMESTAMPTZ,
    deleted_by          VARCHAR(64),
    CONSTRAINT fk_ptins_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_ptins_status ON ptins (status);
CREATE INDEX idx_ptins_user_id ON ptins (user_id);
