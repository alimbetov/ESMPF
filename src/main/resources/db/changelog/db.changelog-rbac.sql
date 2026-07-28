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
    CONSTRAINT fk_access_role_business FOREIGN KEY (business_id) REFERENCES business(id),
    CONSTRAINT uk_access_role_business_id_id UNIQUE (business_id, id),
    CONSTRAINT ck_access_role_code CHECK (code ~ '^[A-Z][A-Z0-9_]{1,79}$')
);
CREATE UNIQUE INDEX uk_access_role_business_code ON access_role (business_id, lower(code));
CREATE INDEX idx_access_role_business ON access_role (business_id);
CREATE INDEX idx_access_role_business_active ON access_role (business_id, active);
--rollback DROP TABLE access_role CASCADE;

--changeset esmpf:181-rbac-permission-assignment-foundation dbms:postgresql
CREATE TABLE permission (
    code VARCHAR(100) PRIMARY KEY,
    category VARCHAR(50) NOT NULL,
    description VARCHAR(300) NOT NULL,
    CONSTRAINT ck_permission_code CHECK (code ~ '^[A-Z][A-Z0-9_]{1,99}$')
);

CREATE TABLE role_permission (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    role_id UUID NOT NULL,
    permission_code VARCHAR(100) NOT NULL,
    granted_by UUID,
    granted_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_role_permission_role FOREIGN KEY (role_id) REFERENCES access_role(id),
    CONSTRAINT fk_role_permission_permission FOREIGN KEY (permission_code) REFERENCES permission(code),
    CONSTRAINT uk_role_permission UNIQUE (role_id, permission_code)
);
CREATE INDEX idx_role_permission_role ON role_permission(role_id);
CREATE INDEX idx_role_permission_code ON role_permission(permission_code);

CREATE TABLE user_role_assignment (
    id UUID PRIMARY KEY,
    business_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    valid_from TIMESTAMPTZ,
    valid_until TIMESTAMPTZ,
    assigned_by UUID NOT NULL,
    assigned_at TIMESTAMPTZ NOT NULL,
    revoked_by UUID,
    revoked_at TIMESTAMPTZ,
    CONSTRAINT fk_user_role_assignment_business FOREIGN KEY (business_id) REFERENCES business(id),
    CONSTRAINT fk_user_role_assignment_user_tenant FOREIGN KEY (business_id, user_id)
        REFERENCES user_account(business_id, id),
    CONSTRAINT fk_user_role_assignment_role_tenant FOREIGN KEY (business_id, role_id)
        REFERENCES access_role(business_id, id),
    CONSTRAINT fk_user_role_assignment_assigned_by_tenant FOREIGN KEY (business_id, assigned_by)
        REFERENCES user_account(business_id, id),
    CONSTRAINT fk_user_role_assignment_revoked_by_tenant FOREIGN KEY (business_id, revoked_by)
        REFERENCES user_account(business_id, id),
    CONSTRAINT ck_user_role_assignment_status CHECK (status IN ('ACTIVE','REVOKED')),
    CONSTRAINT ck_user_role_assignment_dates CHECK (valid_until IS NULL OR valid_from IS NULL OR valid_until > valid_from),
    CONSTRAINT ck_user_role_assignment_revoke_state CHECK (
        (status='ACTIVE' AND revoked_at IS NULL AND revoked_by IS NULL)
        OR (status='REVOKED' AND revoked_at IS NOT NULL AND revoked_by IS NOT NULL)
    )
);
CREATE UNIQUE INDEX uk_user_role_assignment_active
    ON user_role_assignment(business_id, user_id, role_id) WHERE status='ACTIVE';
CREATE INDEX idx_user_role_assignment_effective
    ON user_role_assignment(business_id, user_id, status, valid_from, valid_until);

CREATE UNIQUE INDEX uk_user_account_external_identity_global
    ON user_account(external_provider, external_subject)
    WHERE external_provider IS NOT NULL AND external_subject IS NOT NULL;

INSERT INTO permission(code, category, description)
SELECT value,
       split_part(value, '_', 1),
       replace(initcap(lower(value)), '_', ' ')
FROM unnest(string_to_array(
'BUSINESS_READ,BUSINESS_WRITE,BUSINESS_LIFECYCLE,LOCATION_READ,LOCATION_WRITE,USER_READ,USER_WRITE,USER_LIFECYCLE,QUALIFICATION_READ,QUALIFICATION_WRITE,ROLE_READ,ROLE_WRITE,ROLE_ASSIGN,PERMISSION_READ,CUSTOMER_READ,CUSTOMER_WRITE,CUSTOMER_ARCHIVE,CUSTOMER_INTERACTION_READ,CUSTOMER_INTERACTION_WRITE,CATALOG_READ,CATALOG_WRITE,CATALOG_PUBLISH,EQUIPMENT_READ,EQUIPMENT_WRITE,EQUIPMENT_ARCHIVE,EQUIPMENT_RELATION_WRITE,EQUIPMENT_ISSUE_WRITE,METER_READING_WRITE,MAINTENANCE_PLAN_READ,MAINTENANCE_PLAN_WRITE,MAINTENANCE_OCCURRENCE_READ,MAINTENANCE_OCCURRENCE_WRITE,SERVICE_REQUEST_READ,SERVICE_REQUEST_WRITE,SERVICE_REQUEST_DECIDE,SERVICE_REQUEST_CONVERT,SERVICE_JOB_READ,SERVICE_JOB_WRITE,SERVICE_JOB_DISPATCH,SERVICE_JOB_EXECUTE,SERVICE_JOB_CLOSE,JOB_VISIT_READ,JOB_VISIT_PLAN,JOB_VISIT_EXECUTE,WORK_EXECUTION_READ,WORK_EXECUTION_EXECUTE,WORK_REPORT_READ,WORK_REPORT_WRITE,WORK_REPORT_APPROVE,RECOMMENDATION_READ,RECOMMENDATION_WRITE,MATERIAL_READ,MATERIAL_WRITE,SERVICE_AGREEMENT_READ,SERVICE_AGREEMENT_WRITE,WARRANTY_READ,WARRANTY_DECIDE,DEVICE_SELF_MANAGE,DEVICE_ADMIN,ESTIMATE_READ,ESTIMATE_WRITE,ESTIMATE_SEND,ESTIMATE_DECIDE,REPORT_TEMPLATE_READ,REPORT_TEMPLATE_WRITE,REPORT_TEMPLATE_PUBLISH,DOCUMENT_READ,DOCUMENT_GENERATE,DOCUMENT_DELIVER,DOCUMENT_SIGN,ATTACHMENT_READ,ATTACHMENT_LINK,ATTACHMENT_LIFECYCLE,NOTIFICATION_TEMPLATE_READ,NOTIFICATION_TEMPLATE_WRITE,NOTIFICATION_SEND,NOTIFICATION_READ,FEEDBACK_READ,FEEDBACK_WRITE,CONTENT_READ,CONTENT_WRITE,CONTENT_PUBLISH,DATA_JOB_CREATE,DATA_JOB_READ,INTEGRATION_READ,INTEGRATION_WRITE,AUDIT_READ,PUBLIC_TOKEN_MANAGE,DOCUMENT_SEQUENCE_ALLOCATE', ',')) AS value;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM user_account WHERE upper(role) NOT IN
        ('USER','VIEWER','TECHNICIAN','SUPERVISOR','DISPATCHER','ADMIN','OWNER')) THEN
        RAISE EXCEPTION 'Unknown legacy user_account.role values prevent deterministic RBAC migration';
    END IF;
END $$;

INSERT INTO access_role(id,business_id,created_at,updated_at,version,code,name,description,system,active)
SELECT gen_random_uuid(), b.id, now(), now(), 0, r.code, r.name, r.description, true, true
FROM business b
CROSS JOIN (VALUES
 ('OWNER','Owner','Full tenant control'),
 ('ADMIN','Administrator','Tenant administration'),
 ('DISPATCHER','Dispatcher','Request and work dispatch'),
 ('SUPERVISOR','Supervisor','Operational supervision'),
 ('TECHNICIAN','Technician','Assigned service execution'),
 ('VIEWER','Viewer','Read-only tenant access')
) AS r(code,name,description)
ON CONFLICT DO NOTHING;

INSERT INTO role_permission(id,created_at,updated_at,version,role_id,permission_code,granted_at)
SELECT gen_random_uuid(), now(), now(), 0, r.id, p.code, now()
FROM access_role r CROSS JOIN permission p
WHERE r.code='OWNER'
ON CONFLICT DO NOTHING;

INSERT INTO role_permission(id,created_at,updated_at,version,role_id,permission_code,granted_at)
SELECT gen_random_uuid(), now(), now(), 0, r.id, p.code, now()
FROM access_role r JOIN permission p ON p.code LIKE '%\_READ' ESCAPE '\'
WHERE r.code='VIEWER'
ON CONFLICT DO NOTHING;

INSERT INTO user_role_assignment(
 id,business_id,created_at,updated_at,version,user_id,role_id,status,
 assigned_by,assigned_at)
SELECT gen_random_uuid(), u.business_id, now(), now(), 0, u.id, r.id, 'ACTIVE', u.id, now()
FROM user_account u
JOIN access_role r ON r.business_id=u.business_id
 AND r.code=CASE upper(u.role) WHEN 'USER' THEN 'VIEWER' ELSE upper(u.role) END
ON CONFLICT DO NOTHING;

--rollback DROP INDEX IF EXISTS uk_user_account_external_identity_global; DROP TABLE user_role_assignment; DROP TABLE role_permission; DROP TABLE permission;
