--liquibase formatted sql

--changeset esmpf:180-rbac-role-foundation dbms:postgresql
CREATE TABLE access_role (
    id UUID PRIMARY KEY,
    business_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    code VARCHAR(80) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    system BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_access_role_business
        FOREIGN KEY (business_id) REFERENCES business(id),
    CONSTRAINT uk_access_role_business_id_id
        UNIQUE (business_id, id),
    CONSTRAINT ck_access_role_code
        CHECK (code ~ '^[A-Z][A-Z0-9_]{1,79}$')
);

CREATE UNIQUE INDEX uk_access_role_business_code
    ON access_role (business_id, lower(code));

CREATE INDEX idx_access_role_business
    ON access_role (business_id);

CREATE INDEX idx_access_role_business_active
    ON access_role (business_id, active);

--rollback DROP TABLE access_role CASCADE;
