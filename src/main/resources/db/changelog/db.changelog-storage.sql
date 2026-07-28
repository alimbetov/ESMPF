--liquibase formatted sql

--changeset esmpf:220-file-storage-foundation dbms:postgresql
CREATE TABLE stored_file (
    id UUID PRIMARY KEY,
    business_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    original_file_name VARCHAR(255) NOT NULL,
    normalized_file_name VARCHAR(255) NOT NULL,
    storage_key VARCHAR(512),
    declared_mime_type VARCHAR(127),
    detected_mime_type VARCHAR(127) NOT NULL,
    file_size BIGINT NOT NULL DEFAULT 0,
    checksum_sha256 VARCHAR(64),
    storage_provider VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_by UUID NOT NULL,
    available_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID,
    physical_deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_stored_file_business FOREIGN KEY (business_id) REFERENCES business(id),
    CONSTRAINT fk_stored_file_created_by_tenant FOREIGN KEY (business_id, created_by)
        REFERENCES user_account(business_id, id),
    CONSTRAINT fk_stored_file_deleted_by_tenant FOREIGN KEY (business_id, deleted_by)
        REFERENCES user_account(business_id, id),
    CONSTRAINT uk_stored_file_business_id_id UNIQUE (business_id, id),
    CONSTRAINT uk_stored_file_storage_key UNIQUE (storage_key),
    CONSTRAINT ck_stored_file_size CHECK (file_size >= 0),
    CONSTRAINT ck_stored_file_checksum CHECK (checksum_sha256 IS NULL OR checksum_sha256 ~ '^[0-9a-f]{64}$'),
    CONSTRAINT ck_stored_file_provider CHECK (storage_provider IN ('LOCAL','S3')),
    CONSTRAINT ck_stored_file_status CHECK (status IN (
        'CREATING','AVAILABLE','FAILED','DELETED','PURGED',
        'QUARANTINED','SCANNING','INFECTED','REJECTED','SCAN_FAILED'
    )),
    CONSTRAINT ck_stored_file_deleted_state CHECK (
        (status = 'DELETED' AND deleted_at IS NOT NULL AND deleted_by IS NOT NULL)
        OR status <> 'DELETED'
    ),
    CONSTRAINT ck_stored_file_purged_state CHECK (
        (status = 'PURGED' AND physical_deleted_at IS NOT NULL)
        OR status <> 'PURGED'
    )
);
CREATE INDEX idx_stored_file_business_status_created
    ON stored_file(business_id, status, created_at);
CREATE INDEX idx_stored_file_business_checksum
    ON stored_file(business_id, checksum_sha256);
CREATE INDEX idx_stored_file_business_created
    ON stored_file(business_id, created_at);
CREATE INDEX idx_stored_file_recovery
    ON stored_file(status, updated_at);
--rollback DROP TABLE stored_file CASCADE;

--changeset esmpf:221-file-storage-permissions dbms:postgresql
INSERT INTO permission(code, category, description) VALUES
    ('FILE_READ', 'FILE', 'Read file metadata and content'),
    ('FILE_UPLOAD', 'FILE', 'Upload files'),
    ('FILE_DELETE', 'FILE', 'Soft delete files'),
    ('FILE_RESTORE', 'FILE', 'Restore soft deleted files'),
    ('FILE_ADMIN', 'FILE', 'Administer storage lifecycle')
ON CONFLICT (code) DO NOTHING;

INSERT INTO role_permission(id,created_at,updated_at,version,role_id,permission_code,granted_at)
SELECT gen_random_uuid(), now(), now(), 0, r.id, p.code, now()
FROM access_role r
JOIN permission p ON p.code IN ('FILE_READ','FILE_UPLOAD','FILE_DELETE','FILE_RESTORE','FILE_ADMIN')
WHERE r.code IN ('OWNER','ADMIN')
ON CONFLICT DO NOTHING;

INSERT INTO role_permission(id,created_at,updated_at,version,role_id,permission_code,granted_at)
SELECT gen_random_uuid(), now(), now(), 0, r.id, 'FILE_READ', now()
FROM access_role r
WHERE r.code='VIEWER'
ON CONFLICT DO NOTHING;
--rollback DELETE FROM role_permission WHERE permission_code LIKE 'FILE_%'; DELETE FROM permission WHERE code LIKE 'FILE_%';
