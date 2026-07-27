--liquibase formatted sql

--changeset esmpf:001-extensions dbms:postgresql
CREATE EXTENSION IF NOT EXISTS pgcrypto;
--rollback DROP EXTENSION IF EXISTS pgcrypto;

--changeset esmpf:010-identity dbms:postgresql
CREATE TABLE business (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    name VARCHAR(200) NOT NULL,
    code VARCHAR(100) NOT NULL,
    timezone VARCHAR(100) NOT NULL,
    default_language VARCHAR(10) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(40) NOT NULL,
    settings_json JSONB
);
CREATE TABLE business_location (
    id UUID PRIMARY KEY, business_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
    name VARCHAR(200) NOT NULL, address VARCHAR(500), latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION, timezone VARCHAR(100), active BOOLEAN NOT NULL
);
CREATE TABLE user_account (
    id UUID PRIMARY KEY, business_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
    email VARCHAR(320), phone VARCHAR(50), password_hash VARCHAR(255),
    full_name VARCHAR(200) NOT NULL, role VARCHAR(60) NOT NULL,
    worker BOOLEAN NOT NULL, active BOOLEAN NOT NULL,
    external_provider VARCHAR(100), external_subject VARCHAR(255)
);
CREATE TABLE worker_qualification (
    id UUID PRIMARY KEY, business_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
    user_id UUID NOT NULL, type VARCHAR(80) NOT NULL, name VARCHAR(200) NOT NULL,
    issuer VARCHAR(200), reference_number VARCHAR(100), valid_from DATE,
    valid_until DATE, attachment_id UUID, status VARCHAR(40) NOT NULL
);
--rollback DROP TABLE worker_qualification, user_account, business_location, business CASCADE;

--changeset esmpf:020-customer dbms:postgresql
CREATE TABLE customer (
    id UUID PRIMARY KEY, business_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
    type VARCHAR(40) NOT NULL, name VARCHAR(200) NOT NULL,
    primary_phone VARCHAR(50), primary_email VARCHAR(320), preferred_language VARCHAR(10),
    contacts_json JSONB, notification_preferences_json JSONB,
    billing_data_json JSONB, consents_json JSONB, status VARCHAR(40) NOT NULL
);
CREATE TABLE customer_interaction (
    id UUID PRIMARY KEY, business_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
    customer_id UUID NOT NULL, type VARCHAR(60) NOT NULL, subject VARCHAR(300),
    content TEXT, occurred_at TIMESTAMPTZ NOT NULL, created_by UUID,
    related_subject_type VARCHAR(80), related_subject_id UUID
);
CREATE TABLE service_location (
    id UUID PRIMARY KEY, business_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
    customer_id UUID NOT NULL, parent_location_id UUID, name VARCHAR(200) NOT NULL,
    type VARCHAR(60), address VARCHAR(500), latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION, timezone VARCHAR(100),
    access_instructions VARCHAR(1000), status VARCHAR(40) NOT NULL
);
--rollback DROP TABLE service_location, customer_interaction, customer CASCADE;

--changeset esmpf:030-catalog dbms:postgresql
CREATE TABLE equipment_type (
    id UUID PRIMARY KEY, business_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
    code VARCHAR(255), name VARCHAR(255), category VARCHAR(255), schema_version INTEGER,
    attribute_schema_json JSONB, measurement_schema_json JSONB,
    meter_schema_json JSONB, status VARCHAR(255)
);
CREATE TABLE job_type (
    id UUID PRIMARY KEY, business_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
    code VARCHAR(255), name VARCHAR(255), category VARCHAR(255),
    default_duration_minutes INTEGER, default_price NUMERIC(19,4),
    requires_checklist BOOLEAN, requires_signature BOOLEAN,
    requires_pdf_report BOOLEAN, settings_json JSONB, status VARCHAR(255)
);
CREATE TABLE checklist_template (
    id UUID PRIMARY KEY, business_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
    code VARCHAR(255), name VARCHAR(255), equipment_type_id UUID, job_type_id UUID,
    template_version INTEGER, schema_json JSONB, status VARCHAR(255), published_at TIMESTAMPTZ
);
CREATE TABLE maintenance_template (
    id UUID PRIMARY KEY, business_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
    code VARCHAR(255), name VARCHAR(255), equipment_type_id UUID, job_type_id UUID,
    checklist_template_id UUID, template_version INTEGER, schedule_rule_json JSONB,
    reminder_rule_json JSONB, settings_json JSONB, status VARCHAR(255)
);
CREATE TABLE unit_of_measure (
    id UUID PRIMARY KEY, business_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
    code VARCHAR(255), symbol VARCHAR(255), name VARCHAR(255),
    quantity_type VARCHAR(255), precision_scale INTEGER, active BOOLEAN
);
--rollback DROP TABLE unit_of_measure, maintenance_template, checklist_template, job_type, equipment_type CASCADE;

--changeset esmpf:040-equipment dbms:postgresql
CREATE TABLE equipment (
    id UUID PRIMARY KEY, business_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
    customer_id UUID NOT NULL, service_location_id UUID NOT NULL,
    equipment_type_id UUID NOT NULL, parent_equipment_id UUID,
    name VARCHAR(200) NOT NULL, manufacturer VARCHAR(200), model VARCHAR(200),
    serial_number VARCHAR(150), asset_number VARCHAR(150), status VARCHAR(40) NOT NULL,
    installation_date DATE, commissioning_date DATE, warranty_until DATE,
    attributes_json JSONB, current_meter_values_json JSONB
);
CREATE TABLE equipment_relation (
    id UUID PRIMARY KEY, business_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
    source_equipment_id UUID NOT NULL, target_equipment_id UUID NOT NULL,
    relation_type VARCHAR(80) NOT NULL, valid_from DATE, valid_until DATE, description TEXT
);
CREATE TABLE equipment_issue (
    id UUID PRIMARY KEY, business_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
    equipment_id UUID NOT NULL, detected_by_job_id UUID, type VARCHAR(80) NOT NULL,
    severity VARCHAR(40) NOT NULL, status VARCHAR(40) NOT NULL, description TEXT,
    detected_at TIMESTAMPTZ NOT NULL, due_date DATE, resolved_by_job_id UUID,
    resolved_at TIMESTAMPTZ
);
CREATE TABLE meter_reading (
    id UUID PRIMARY KEY, business_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
    equipment_id UUID NOT NULL, meter_code VARCHAR(100) NOT NULL,
    reading_value NUMERIC(19,4) NOT NULL, unit_code VARCHAR(40) NOT NULL,
    recorded_at TIMESTAMPTZ NOT NULL, recorded_by UUID, source VARCHAR(40) NOT NULL
);
--rollback DROP TABLE meter_reading, equipment_issue, equipment_relation, equipment CASCADE;

--changeset esmpf:050-maintenance dbms:postgresql
CREATE TABLE maintenance_plan (
    id UUID PRIMARY KEY, business_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
    equipment_id UUID NOT NULL, maintenance_template_id UUID NOT NULL,
    template_version INTEGER NOT NULL, active_from DATE NOT NULL, active_until DATE,
    next_due_date DATE, next_due_meter_value NUMERIC(19,4), last_completed_at TIMESTAMPTZ,
    overrides_json JSONB, status VARCHAR(40) NOT NULL
);
CREATE TABLE maintenance_occurrence (
    id UUID PRIMARY KEY, business_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
    maintenance_plan_id UUID NOT NULL, due_date DATE, due_meter_value NUMERIC(19,4),
    status VARCHAR(40) NOT NULL, service_job_id UUID, generated_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ, generation_key VARCHAR(200) NOT NULL, reason VARCHAR(500)
);
--rollback DROP TABLE maintenance_occurrence, maintenance_plan CASCADE;

--changeset esmpf:060-service-core dbms:postgresql
CREATE TABLE service_request (
    id UUID PRIMARY KEY, business_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
    customer_id UUID, service_location_id UUID, equipment_id UUID,
    source VARCHAR(255), priority VARCHAR(255), summary VARCHAR(255),
    description TEXT, status VARCHAR(255), requested_at TIMESTAMPTZ, requested_by UUID
);
CREATE TABLE service_job (
    id UUID PRIMARY KEY, business_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
    request_id UUID, maintenance_occurrence_id UUID, customer_id UUID,
    service_location_id UUID, equipment_id UUID, job_type_id UUID,
    service_agreement_id UUID, status VARCHAR(255), priority VARCHAR(255),
    title VARCHAR(255), description TEXT, planned_start TIMESTAMPTZ,
    planned_end TIMESTAMPTZ, lead_worker_id UUID,
    assigned_worker_ids_json JSONB, blocked_reason VARCHAR(255)
);
CREATE TABLE job_visit (
    id UUID PRIMARY KEY, business_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
    job_id UUID, scheduled_start TIMESTAMPTZ, scheduled_end TIMESTAMPTZ,
    actual_start TIMESTAMPTZ, actual_end TIMESTAMPTZ, status VARCHAR(255),
    worker_ids_json JSONB, arrival_data_json JSONB, completion_data_json JSONB,
    customer_confirmation_json JSONB
);
CREATE TABLE job_execution (
    id UUID PRIMARY KEY, business_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
    job_id UUID, visit_id UUID, checklist_template_id UUID, template_version INTEGER,
    schema_snapshot_json JSONB, answers_json JSONB, started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ, completed_by UUID, status VARCHAR(255)
);
CREATE TABLE work_report (
    id UUID PRIMARY KEY, business_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
    job_id UUID, visit_id UUID, job_execution_id UUID, diagnosis TEXT,
    work_performed TEXT, result TEXT, materials_summary_json JSONB,
    measurements_summary_json JSONB, customer_comment VARCHAR(255),
    completed_by UUID, completed_at TIMESTAMPTZ, status VARCHAR(255)
);
--rollback DROP TABLE work_report, job_execution, job_visit, service_job, service_request CASCADE;

--changeset esmpf:061-service-support dbms:postgresql
CREATE TABLE recommendation (
    id UUID PRIMARY KEY, business_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
    equipment_id UUID, source_job_id UUID, description TEXT, priority VARCHAR(255),
    due_date DATE, status VARCHAR(255), converted_job_id UUID
);
CREATE TABLE material_catalog_item (
    id UUID PRIMARY KEY, business_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
    code VARCHAR(255), name VARCHAR(255), unit_code VARCHAR(255),
    default_price NUMERIC(19,4), currency VARCHAR(3), active BOOLEAN
);
CREATE TABLE job_material (
    id UUID PRIMARY KEY, business_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
    job_id UUID, material_catalog_item_id UUID, type VARCHAR(255), description TEXT,
    quantity NUMERIC(19,4), unit_code VARCHAR(255), unit_price NUMERIC(19,4),
    currency VARCHAR(3), source VARCHAR(255)
);
CREATE TABLE service_agreement (
    id UUID PRIMARY KEY, business_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
    customer_id UUID, number VARCHAR(255), type VARCHAR(255), status VARCHAR(255),
    valid_from DATE, valid_until DATE, covered_equipment_ids_json JSONB,
    coverage_rules_json JSONB, sla_rules_json JSONB, pricing_rules_json JSONB,
    attachment_id UUID
);
CREATE TABLE warranty_case (
    id UUID PRIMARY KEY, business_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
    equipment_id UUID, job_id UUID, source VARCHAR(255), status VARCHAR(255),
    description TEXT, decision VARCHAR(255), opened_at TIMESTAMPTZ, resolved_at TIMESTAMPTZ
);
CREATE TABLE mobile_device (
    id UUID PRIMARY KEY, business_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
    user_id UUID, device_identifier VARCHAR(255), platform VARCHAR(255),
    app_version VARCHAR(255), status VARCHAR(255), last_seen_at TIMESTAMPTZ,
    registered_at TIMESTAMPTZ
);
CREATE TABLE sync_operation (
    id UUID PRIMARY KEY, business_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
    device_id UUID, client_operation_id VARCHAR(255), operation_type VARCHAR(255),
    subject_type VARCHAR(255), subject_id UUID, payload_hash VARCHAR(255),
    status VARCHAR(255), occurred_at TIMESTAMPTZ, received_at TIMESTAMPTZ,
    error_code VARCHAR(255)
);
--rollback DROP TABLE sync_operation, mobile_device, warranty_case, service_agreement, job_material, material_catalog_item, recommendation CASCADE;

--changeset esmpf:070-commercial dbms:postgresql
CREATE TABLE estimate (
    id UUID PRIMARY KEY, business_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
    job_id UUID NOT NULL, number VARCHAR(100) NOT NULL, status VARCHAR(40) NOT NULL,
    currency VARCHAR(3) NOT NULL, lines_json JSONB NOT NULL,
    subtotal NUMERIC(19,4) NOT NULL, discount NUMERIC(19,4) NOT NULL,
    tax NUMERIC(19,4) NOT NULL, total NUMERIC(19,4) NOT NULL,
    approval_data_json JSONB, approved_at TIMESTAMPTZ
);
CREATE TABLE invoice (
    id UUID PRIMARY KEY, business_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
    job_id UUID NOT NULL, estimate_id UUID, number VARCHAR(100) NOT NULL,
    status VARCHAR(40) NOT NULL, currency VARCHAR(3) NOT NULL,
    lines_json JSONB NOT NULL, subtotal NUMERIC(19,4) NOT NULL,
    tax NUMERIC(19,4) NOT NULL, total NUMERIC(19,4) NOT NULL,
    paid_amount NUMERIC(19,4) NOT NULL, due_date DATE,
    external_accounting_id VARCHAR(200), generated_document_id UUID
);
CREATE TABLE payment (
    id UUID PRIMARY KEY, business_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
    invoice_id UUID NOT NULL, amount NUMERIC(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL, method VARCHAR(60) NOT NULL,
    status VARCHAR(40) NOT NULL, paid_at TIMESTAMPTZ,
    external_payment_id VARCHAR(200), details_json JSONB
);
--rollback DROP TABLE payment, invoice, estimate CASCADE;

--changeset esmpf:080-document dbms:postgresql
CREATE TABLE report_template (
    id UUID PRIMARY KEY, business_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
    code VARCHAR(100) NOT NULL, document_type VARCHAR(80) NOT NULL,
    locale VARCHAR(20) NOT NULL, template_version INTEGER NOT NULL,
    template_content TEXT NOT NULL, stylesheet_content TEXT,
    configuration_json JSONB, status VARCHAR(40) NOT NULL, published_at TIMESTAMPTZ
);
CREATE TABLE generated_document (
    id UUID PRIMARY KEY, business_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
    document_type VARCHAR(80) NOT NULL, document_number VARCHAR(120) NOT NULL,
    source_type VARCHAR(80) NOT NULL, source_id UUID NOT NULL,
    report_template_id UUID NOT NULL, snapshot_json JSONB NOT NULL,
    status VARCHAR(40) NOT NULL, attachment_id UUID, checksum VARCHAR(128),
    generation_attempts INTEGER NOT NULL, last_error TEXT, generated_at TIMESTAMPTZ,
    supersedes_document_id UUID, delivery_data_json JSONB
);
CREATE TABLE document_signature (
    id UUID PRIMARY KEY, business_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
    generated_document_id UUID NOT NULL, signer_type VARCHAR(60) NOT NULL,
    signer_name VARCHAR(200) NOT NULL, signer_user_id UUID,
    method VARCHAR(60) NOT NULL, signature_attachment_id UUID,
    signed_at TIMESTAMPTZ NOT NULL, ip_address VARCHAR(64),
    user_agent VARCHAR(1000), verification_data_json JSONB
);
CREATE TABLE attachment (
    id UUID PRIMARY KEY, business_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
    storage_key VARCHAR(500) NOT NULL, file_name VARCHAR(500) NOT NULL,
    content_type VARCHAR(200) NOT NULL, size_bytes BIGINT NOT NULL,
    checksum VARCHAR(128) NOT NULL, metadata_json JSONB, created_by UUID,
    status VARCHAR(40) NOT NULL
);
CREATE TABLE attachment_link (
    id UUID PRIMARY KEY, business_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
    attachment_id UUID NOT NULL, subject_type VARCHAR(80) NOT NULL,
    subject_id UUID NOT NULL, purpose VARCHAR(80) NOT NULL
);
--rollback DROP TABLE attachment_link, document_signature, generated_document, report_template, attachment CASCADE;

--changeset esmpf:090-communication dbms:postgresql
CREATE TABLE notification_template (
    id UUID PRIMARY KEY, business_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
    code VARCHAR(100) NOT NULL, channel VARCHAR(40) NOT NULL,
    locale VARCHAR(20) NOT NULL, template_version INTEGER NOT NULL,
    subject_template VARCHAR(500), body_template TEXT NOT NULL,
    status VARCHAR(40) NOT NULL
);
CREATE TABLE notification (
    id UUID PRIMARY KEY, business_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
    customer_id UUID, recipient VARCHAR(500) NOT NULL, channel VARCHAR(40) NOT NULL,
    notification_template_id UUID, payload_json JSONB NOT NULL,
    status VARCHAR(40) NOT NULL, attempt_count INTEGER NOT NULL,
    next_attempt_at TIMESTAMPTZ, sent_at TIMESTAMPTZ,
    provider_message_id VARCHAR(255), last_error TEXT
);
CREATE TABLE customer_feedback (
    id UUID PRIMARY KEY, business_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
    customer_id UUID NOT NULL, job_id UUID, type VARCHAR(60) NOT NULL,
    rating INTEGER, comment TEXT, publication_consent BOOLEAN NOT NULL,
    status VARCHAR(40) NOT NULL, company_response TEXT, responded_by UUID,
    responded_at TIMESTAMPTZ, resolved_at TIMESTAMPTZ
);
--rollback DROP TABLE customer_feedback, notification, notification_template CASCADE;

--changeset esmpf:100-platform dbms:postgresql
CREATE TABLE public_access_token (
    id UUID PRIMARY KEY, business_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
    purpose VARCHAR(80) NOT NULL, subject_type VARCHAR(80) NOT NULL,
    subject_id UUID NOT NULL, token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL, max_uses INTEGER,
    used_count INTEGER NOT NULL, revoked_at TIMESTAMPTZ
);
CREATE TABLE data_job (
    id UUID PRIMARY KEY, business_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
    type VARCHAR(80) NOT NULL, format VARCHAR(40) NOT NULL,
    subject_type VARCHAR(80), status VARCHAR(40) NOT NULL,
    source_attachment_id UUID, result_attachment_id UUID,
    configuration_json JSONB, progress INTEGER NOT NULL,
    errors_json JSONB, completed_at TIMESTAMPTZ
);
CREATE TABLE outbox_event (
    id UUID PRIMARY KEY, business_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
    aggregate_type VARCHAR(100) NOT NULL, aggregate_id UUID NOT NULL,
    event_type VARCHAR(150) NOT NULL, event_version INTEGER NOT NULL,
    payload_json JSONB NOT NULL, status VARCHAR(40) NOT NULL,
    published_at TIMESTAMPTZ, attempt_count INTEGER NOT NULL,
    next_attempt_at TIMESTAMPTZ, last_error TEXT
);
CREATE TABLE audit_log (
    id UUID PRIMARY KEY, business_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
    actor_type VARCHAR(80) NOT NULL, actor_id UUID, action VARCHAR(150) NOT NULL,
    subject_type VARCHAR(100) NOT NULL, subject_id UUID NOT NULL,
    before_data_json JSONB, after_data_json JSONB, metadata_json JSONB,
    occurred_at TIMESTAMPTZ NOT NULL
);
CREATE TABLE idempotency_record (
    id UUID PRIMARY KEY, business_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
    idempotency_key VARCHAR(200) NOT NULL, operation VARCHAR(150) NOT NULL,
    request_hash VARCHAR(128) NOT NULL, response_reference VARCHAR(500),
    status VARCHAR(40) NOT NULL, expires_at TIMESTAMPTZ NOT NULL
);
CREATE TABLE integration_connection (
    id UUID PRIMARY KEY, business_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
    type VARCHAR(80) NOT NULL, name VARCHAR(200) NOT NULL,
    status VARCHAR(40) NOT NULL, configuration_json JSONB,
    secret_reference VARCHAR(500), last_successful_at TIMESTAMPTZ,
    last_error_at TIMESTAMPTZ
);
CREATE TABLE document_sequence (
    id UUID PRIMARY KEY, business_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
    document_type VARCHAR(80) NOT NULL, sequence_year INTEGER NOT NULL,
    prefix VARCHAR(40), current_value BIGINT NOT NULL
);
--rollback DROP TABLE document_sequence, integration_connection, idempotency_record, audit_log, outbox_event, data_job, public_access_token CASCADE;

--changeset esmpf:110-tenant-integrity dbms:postgresql splitStatements:false
DO $$
DECLARE
    t TEXT;
BEGIN
    FOREACH t IN ARRAY ARRAY[
        'business_location','user_account','worker_qualification','customer','customer_interaction',
        'service_location','equipment_type','job_type','checklist_template','maintenance_template',
        'unit_of_measure','equipment','equipment_relation','equipment_issue','meter_reading',
        'maintenance_plan','maintenance_occurrence','service_request','service_job','job_visit',
        'job_execution','work_report','recommendation','material_catalog_item','job_material',
        'service_agreement','warranty_case','mobile_device','sync_operation','estimate','invoice',
        'payment','report_template','generated_document','document_signature','attachment',
        'attachment_link','notification_template','notification','customer_feedback',
        'public_access_token','data_job','outbox_event','audit_log','idempotency_record',
        'integration_connection','document_sequence'
    ]
    LOOP
        EXECUTE format('ALTER TABLE %I ADD CONSTRAINT %I FOREIGN KEY (business_id) REFERENCES business(id)', t, 'fk_' || t || '_business');
        EXECUTE format('ALTER TABLE %I ADD CONSTRAINT %I UNIQUE (business_id,id)', t, 'uk_' || t || '_business_id_id');
    END LOOP;
END $$;
--rollback SELECT 1;

--changeset esmpf:120-cross-module-fks dbms:postgresql
ALTER TABLE worker_qualification ADD CONSTRAINT fk_worker_qualification_user FOREIGN KEY (business_id,user_id) REFERENCES user_account(business_id,id);
ALTER TABLE customer_interaction ADD CONSTRAINT fk_customer_interaction_customer FOREIGN KEY (business_id,customer_id) REFERENCES customer(business_id,id);
ALTER TABLE service_location ADD CONSTRAINT fk_service_location_customer FOREIGN KEY (business_id,customer_id) REFERENCES customer(business_id,id);
ALTER TABLE service_location ADD CONSTRAINT fk_service_location_parent FOREIGN KEY (business_id,parent_location_id) REFERENCES service_location(business_id,id);
ALTER TABLE checklist_template ADD CONSTRAINT fk_checklist_equipment_type FOREIGN KEY (business_id,equipment_type_id) REFERENCES equipment_type(business_id,id);
ALTER TABLE checklist_template ADD CONSTRAINT fk_checklist_job_type FOREIGN KEY (business_id,job_type_id) REFERENCES job_type(business_id,id);
ALTER TABLE maintenance_template ADD CONSTRAINT fk_maintenance_template_equipment_type FOREIGN KEY (business_id,equipment_type_id) REFERENCES equipment_type(business_id,id);
ALTER TABLE maintenance_template ADD CONSTRAINT fk_maintenance_template_job_type FOREIGN KEY (business_id,job_type_id) REFERENCES job_type(business_id,id);
ALTER TABLE maintenance_template ADD CONSTRAINT fk_maintenance_template_checklist FOREIGN KEY (business_id,checklist_template_id) REFERENCES checklist_template(business_id,id);
ALTER TABLE equipment ADD CONSTRAINT fk_equipment_customer FOREIGN KEY (business_id,customer_id) REFERENCES customer(business_id,id);
ALTER TABLE equipment ADD CONSTRAINT fk_equipment_location FOREIGN KEY (business_id,service_location_id) REFERENCES service_location(business_id,id);
ALTER TABLE equipment ADD CONSTRAINT fk_equipment_type FOREIGN KEY (business_id,equipment_type_id) REFERENCES equipment_type(business_id,id);
ALTER TABLE equipment ADD CONSTRAINT fk_equipment_parent FOREIGN KEY (business_id,parent_equipment_id) REFERENCES equipment(business_id,id);
ALTER TABLE equipment_relation ADD CONSTRAINT fk_equipment_relation_source FOREIGN KEY (business_id,source_equipment_id) REFERENCES equipment(business_id,id);
ALTER TABLE equipment_relation ADD CONSTRAINT fk_equipment_relation_target FOREIGN KEY (business_id,target_equipment_id) REFERENCES equipment(business_id,id);
ALTER TABLE equipment_issue ADD CONSTRAINT fk_equipment_issue_equipment FOREIGN KEY (business_id,equipment_id) REFERENCES equipment(business_id,id);
ALTER TABLE meter_reading ADD CONSTRAINT fk_meter_reading_equipment FOREIGN KEY (business_id,equipment_id) REFERENCES equipment(business_id,id);
ALTER TABLE maintenance_plan ADD CONSTRAINT fk_maintenance_plan_equipment FOREIGN KEY (business_id,equipment_id) REFERENCES equipment(business_id,id);
ALTER TABLE maintenance_plan ADD CONSTRAINT fk_maintenance_plan_template FOREIGN KEY (business_id,maintenance_template_id) REFERENCES maintenance_template(business_id,id);
ALTER TABLE maintenance_occurrence ADD CONSTRAINT fk_occurrence_plan FOREIGN KEY (business_id,maintenance_plan_id) REFERENCES maintenance_plan(business_id,id);
ALTER TABLE service_request ADD CONSTRAINT fk_service_request_customer FOREIGN KEY (business_id,customer_id) REFERENCES customer(business_id,id);
ALTER TABLE service_request ADD CONSTRAINT fk_service_request_location FOREIGN KEY (business_id,service_location_id) REFERENCES service_location(business_id,id);
ALTER TABLE service_request ADD CONSTRAINT fk_service_request_equipment FOREIGN KEY (business_id,equipment_id) REFERENCES equipment(business_id,id);
ALTER TABLE service_job ADD CONSTRAINT fk_service_job_request FOREIGN KEY (business_id,request_id) REFERENCES service_request(business_id,id);
ALTER TABLE service_job ADD CONSTRAINT fk_service_job_occurrence FOREIGN KEY (business_id,maintenance_occurrence_id) REFERENCES maintenance_occurrence(business_id,id);
ALTER TABLE service_job ADD CONSTRAINT fk_service_job_customer FOREIGN KEY (business_id,customer_id) REFERENCES customer(business_id,id);
ALTER TABLE service_job ADD CONSTRAINT fk_service_job_location FOREIGN KEY (business_id,service_location_id) REFERENCES service_location(business_id,id);
ALTER TABLE service_job ADD CONSTRAINT fk_service_job_equipment FOREIGN KEY (business_id,equipment_id) REFERENCES equipment(business_id,id);
ALTER TABLE service_job ADD CONSTRAINT fk_service_job_type FOREIGN KEY (business_id,job_type_id) REFERENCES job_type(business_id,id);
ALTER TABLE job_visit ADD CONSTRAINT fk_job_visit_job FOREIGN KEY (business_id,job_id) REFERENCES service_job(business_id,id);
ALTER TABLE job_execution ADD CONSTRAINT fk_job_execution_job FOREIGN KEY (business_id,job_id) REFERENCES service_job(business_id,id);
ALTER TABLE job_execution ADD CONSTRAINT fk_job_execution_visit FOREIGN KEY (business_id,visit_id) REFERENCES job_visit(business_id,id);
ALTER TABLE job_execution ADD CONSTRAINT fk_job_execution_checklist FOREIGN KEY (business_id,checklist_template_id) REFERENCES checklist_template(business_id,id);
ALTER TABLE work_report ADD CONSTRAINT fk_work_report_job FOREIGN KEY (business_id,job_id) REFERENCES service_job(business_id,id);
ALTER TABLE work_report ADD CONSTRAINT fk_work_report_visit FOREIGN KEY (business_id,visit_id) REFERENCES job_visit(business_id,id);
ALTER TABLE work_report ADD CONSTRAINT fk_work_report_execution FOREIGN KEY (business_id,job_execution_id) REFERENCES job_execution(business_id,id);
ALTER TABLE recommendation ADD CONSTRAINT fk_recommendation_equipment FOREIGN KEY (business_id,equipment_id) REFERENCES equipment(business_id,id);
ALTER TABLE recommendation ADD CONSTRAINT fk_recommendation_source_job FOREIGN KEY (business_id,source_job_id) REFERENCES service_job(business_id,id);
ALTER TABLE recommendation ADD CONSTRAINT fk_recommendation_converted_job FOREIGN KEY (business_id,converted_job_id) REFERENCES service_job(business_id,id);
ALTER TABLE job_material ADD CONSTRAINT fk_job_material_job FOREIGN KEY (business_id,job_id) REFERENCES service_job(business_id,id);
ALTER TABLE job_material ADD CONSTRAINT fk_job_material_catalog FOREIGN KEY (business_id,material_catalog_item_id) REFERENCES material_catalog_item(business_id,id);
ALTER TABLE service_agreement ADD CONSTRAINT fk_service_agreement_customer FOREIGN KEY (business_id,customer_id) REFERENCES customer(business_id,id);
ALTER TABLE warranty_case ADD CONSTRAINT fk_warranty_equipment FOREIGN KEY (business_id,equipment_id) REFERENCES equipment(business_id,id);
ALTER TABLE warranty_case ADD CONSTRAINT fk_warranty_job FOREIGN KEY (business_id,job_id) REFERENCES service_job(business_id,id);
ALTER TABLE mobile_device ADD CONSTRAINT fk_mobile_device_user FOREIGN KEY (business_id,user_id) REFERENCES user_account(business_id,id);
ALTER TABLE sync_operation ADD CONSTRAINT fk_sync_device FOREIGN KEY (business_id,device_id) REFERENCES mobile_device(business_id,id);
ALTER TABLE estimate ADD CONSTRAINT fk_estimate_job FOREIGN KEY (business_id,job_id) REFERENCES service_job(business_id,id);
ALTER TABLE invoice ADD CONSTRAINT fk_invoice_job FOREIGN KEY (business_id,job_id) REFERENCES service_job(business_id,id);
ALTER TABLE invoice ADD CONSTRAINT fk_invoice_estimate FOREIGN KEY (business_id,estimate_id) REFERENCES estimate(business_id,id);
ALTER TABLE payment ADD CONSTRAINT fk_payment_invoice FOREIGN KEY (business_id,invoice_id) REFERENCES invoice(business_id,id);
ALTER TABLE generated_document ADD CONSTRAINT fk_generated_document_template FOREIGN KEY (business_id,report_template_id) REFERENCES report_template(business_id,id);
ALTER TABLE generated_document ADD CONSTRAINT fk_generated_document_attachment FOREIGN KEY (business_id,attachment_id) REFERENCES attachment(business_id,id);
ALTER TABLE generated_document ADD CONSTRAINT fk_generated_document_supersedes FOREIGN KEY (business_id,supersedes_document_id) REFERENCES generated_document(business_id,id);
ALTER TABLE document_signature ADD CONSTRAINT fk_signature_document FOREIGN KEY (business_id,generated_document_id) REFERENCES generated_document(business_id,id);
ALTER TABLE document_signature ADD CONSTRAINT fk_signature_attachment FOREIGN KEY (business_id,signature_attachment_id) REFERENCES attachment(business_id,id);
ALTER TABLE attachment_link ADD CONSTRAINT fk_attachment_link_attachment FOREIGN KEY (business_id,attachment_id) REFERENCES attachment(business_id,id);
ALTER TABLE notification ADD CONSTRAINT fk_notification_customer FOREIGN KEY (business_id,customer_id) REFERENCES customer(business_id,id);
ALTER TABLE notification ADD CONSTRAINT fk_notification_template FOREIGN KEY (business_id,notification_template_id) REFERENCES notification_template(business_id,id);
ALTER TABLE customer_feedback ADD CONSTRAINT fk_feedback_customer FOREIGN KEY (business_id,customer_id) REFERENCES customer(business_id,id);
ALTER TABLE customer_feedback ADD CONSTRAINT fk_feedback_job FOREIGN KEY (business_id,job_id) REFERENCES service_job(business_id,id);
ALTER TABLE data_job ADD CONSTRAINT fk_data_job_source_attachment FOREIGN KEY (business_id,source_attachment_id) REFERENCES attachment(business_id,id);
ALTER TABLE data_job ADD CONSTRAINT fk_data_job_result_attachment FOREIGN KEY (business_id,result_attachment_id) REFERENCES attachment(business_id,id);
--rollback SELECT 1;

--changeset esmpf:130-uniques dbms:postgresql
ALTER TABLE business ADD CONSTRAINT uk_business_code UNIQUE (code);
CREATE UNIQUE INDEX uk_user_account_business_email ON user_account (business_id,lower(email)) WHERE email IS NOT NULL;
CREATE UNIQUE INDEX uk_user_account_business_phone ON user_account (business_id,phone) WHERE phone IS NOT NULL;
CREATE UNIQUE INDEX uk_user_account_external_identity ON user_account (business_id,external_provider,external_subject) WHERE external_provider IS NOT NULL AND external_subject IS NOT NULL;
CREATE UNIQUE INDEX uk_equipment_type_code ON equipment_type (business_id,lower(code)) WHERE code IS NOT NULL;
CREATE UNIQUE INDEX uk_job_type_code ON job_type (business_id,lower(code)) WHERE code IS NOT NULL;
CREATE UNIQUE INDEX uk_checklist_template_version ON checklist_template (business_id,lower(code),template_version) WHERE code IS NOT NULL AND template_version IS NOT NULL;
CREATE UNIQUE INDEX uk_maintenance_template_version ON maintenance_template (business_id,lower(code),template_version) WHERE code IS NOT NULL AND template_version IS NOT NULL;
CREATE UNIQUE INDEX uk_unit_of_measure_code ON unit_of_measure (business_id,lower(code)) WHERE code IS NOT NULL;
CREATE UNIQUE INDEX uk_equipment_serial ON equipment (business_id,serial_number) WHERE serial_number IS NOT NULL;
CREATE UNIQUE INDEX uk_equipment_asset ON equipment (business_id,asset_number) WHERE asset_number IS NOT NULL;
CREATE UNIQUE INDEX uk_maintenance_occurrence_generation ON maintenance_occurrence (business_id,generation_key);
CREATE UNIQUE INDEX uk_service_job_request ON service_job (business_id,request_id) WHERE request_id IS NOT NULL;
CREATE UNIQUE INDEX uk_service_job_occurrence ON service_job (business_id,maintenance_occurrence_id) WHERE maintenance_occurrence_id IS NOT NULL;
CREATE UNIQUE INDEX uk_material_catalog_item_code ON material_catalog_item (business_id,lower(code)) WHERE code IS NOT NULL;
CREATE UNIQUE INDEX uk_service_agreement_number ON service_agreement (business_id,lower(number)) WHERE number IS NOT NULL;
CREATE UNIQUE INDEX uk_mobile_device_identifier ON mobile_device (business_id,device_identifier) WHERE device_identifier IS NOT NULL;
CREATE UNIQUE INDEX uk_sync_operation_client ON sync_operation (business_id,device_id,client_operation_id) WHERE device_id IS NOT NULL AND client_operation_id IS NOT NULL;
CREATE UNIQUE INDEX uk_estimate_number ON estimate (business_id,lower(number));
CREATE UNIQUE INDEX uk_invoice_number ON invoice (business_id,lower(number));
CREATE UNIQUE INDEX uk_invoice_estimate ON invoice (business_id,estimate_id) WHERE estimate_id IS NOT NULL;
CREATE UNIQUE INDEX uk_payment_external ON payment (business_id,external_payment_id) WHERE external_payment_id IS NOT NULL;
CREATE UNIQUE INDEX uk_report_template_version ON report_template (business_id,lower(code),document_type,locale,template_version);
CREATE UNIQUE INDEX uk_generated_document_number ON generated_document (business_id,document_number);
CREATE UNIQUE INDEX uk_attachment_storage_key ON attachment (business_id,storage_key);
CREATE UNIQUE INDEX uk_attachment_link_identity ON attachment_link (business_id,attachment_id,subject_type,subject_id,purpose);
CREATE UNIQUE INDEX uk_notification_template_version ON notification_template (business_id,lower(code),channel,locale,template_version);
CREATE UNIQUE INDEX uk_public_access_token_hash ON public_access_token (business_id,token_hash);
CREATE UNIQUE INDEX uk_idempotency_operation_key ON idempotency_record (business_id,operation,idempotency_key);
CREATE UNIQUE INDEX uk_integration_connection_name ON integration_connection (business_id,lower(name));
CREATE UNIQUE INDEX uk_document_sequence_key ON document_sequence (business_id,document_type,sequence_year);
--rollback SELECT 1;

--changeset esmpf:140-checks dbms:postgresql
ALTER TABLE business ADD CONSTRAINT ck_business_currency CHECK (currency ~ '^[A-Z]{3}$');
ALTER TABLE business_location ADD CONSTRAINT ck_business_location_lat CHECK (latitude IS NULL OR latitude BETWEEN -90 AND 90);
ALTER TABLE business_location ADD CONSTRAINT ck_business_location_lon CHECK (longitude IS NULL OR longitude BETWEEN -180 AND 180);
ALTER TABLE service_location ADD CONSTRAINT ck_service_location_lat CHECK (latitude IS NULL OR latitude BETWEEN -90 AND 90);
ALTER TABLE service_location ADD CONSTRAINT ck_service_location_lon CHECK (longitude IS NULL OR longitude BETWEEN -180 AND 180);
ALTER TABLE worker_qualification ADD CONSTRAINT ck_worker_qualification_dates CHECK (valid_until IS NULL OR valid_from IS NULL OR valid_until >= valid_from);
ALTER TABLE job_type ADD CONSTRAINT ck_job_type_duration CHECK (default_duration_minutes IS NULL OR default_duration_minutes > 0);
ALTER TABLE job_type ADD CONSTRAINT ck_job_type_price CHECK (default_price IS NULL OR default_price >= 0);
ALTER TABLE unit_of_measure ADD CONSTRAINT ck_unit_precision CHECK (precision_scale IS NULL OR precision_scale BETWEEN 0 AND 12);
ALTER TABLE equipment ADD CONSTRAINT ck_equipment_commissioning CHECK (commissioning_date IS NULL OR installation_date IS NULL OR commissioning_date >= installation_date);
ALTER TABLE equipment ADD CONSTRAINT ck_equipment_warranty CHECK (warranty_until IS NULL OR installation_date IS NULL OR warranty_until >= installation_date);
ALTER TABLE equipment_relation ADD CONSTRAINT ck_equipment_relation_distinct CHECK (source_equipment_id <> target_equipment_id);
ALTER TABLE equipment_relation ADD CONSTRAINT ck_equipment_relation_dates CHECK (valid_until IS NULL OR valid_from IS NULL OR valid_until >= valid_from);
ALTER TABLE meter_reading ADD CONSTRAINT ck_meter_reading_value CHECK (reading_value >= 0);
ALTER TABLE maintenance_plan ADD CONSTRAINT ck_maintenance_plan_dates CHECK (active_until IS NULL OR active_until >= active_from);
ALTER TABLE maintenance_plan ADD CONSTRAINT ck_maintenance_plan_meter CHECK (next_due_meter_value IS NULL OR next_due_meter_value >= 0);
ALTER TABLE maintenance_occurrence ADD CONSTRAINT ck_occurrence_due CHECK (due_date IS NOT NULL OR due_meter_value IS NOT NULL);
ALTER TABLE service_job ADD CONSTRAINT ck_service_job_plan CHECK (planned_end IS NULL OR planned_start IS NULL OR planned_end >= planned_start);
ALTER TABLE job_visit ADD CONSTRAINT ck_job_visit_schedule CHECK (scheduled_end IS NULL OR scheduled_start IS NULL OR scheduled_end >= scheduled_start);
ALTER TABLE job_visit ADD CONSTRAINT ck_job_visit_actual CHECK (actual_end IS NULL OR actual_start IS NULL OR actual_end >= actual_start);
ALTER TABLE job_material ADD CONSTRAINT ck_job_material_quantity CHECK (quantity IS NULL OR quantity > 0);
ALTER TABLE job_material ADD CONSTRAINT ck_job_material_price CHECK (unit_price IS NULL OR unit_price >= 0);
ALTER TABLE service_agreement ADD CONSTRAINT ck_service_agreement_dates CHECK (valid_until IS NULL OR valid_from IS NULL OR valid_until >= valid_from);
ALTER TABLE estimate ADD CONSTRAINT ck_estimate_amounts CHECK (subtotal >= 0 AND discount >= 0 AND tax >= 0 AND total >= 0);
ALTER TABLE estimate ADD CONSTRAINT ck_estimate_total CHECK (total = subtotal - discount + tax);
ALTER TABLE invoice ADD CONSTRAINT ck_invoice_amounts CHECK (subtotal >= 0 AND tax >= 0 AND total >= 0 AND paid_amount >= 0 AND paid_amount <= total);
ALTER TABLE invoice ADD CONSTRAINT ck_invoice_total CHECK (total = subtotal + tax);
ALTER TABLE payment ADD CONSTRAINT ck_payment_amount CHECK (amount > 0);
ALTER TABLE generated_document ADD CONSTRAINT ck_generated_document_attempts CHECK (generation_attempts >= 0);
ALTER TABLE attachment ADD CONSTRAINT ck_attachment_size CHECK (size_bytes >= 0);
ALTER TABLE notification ADD CONSTRAINT ck_notification_attempts CHECK (attempt_count >= 0);
ALTER TABLE customer_feedback ADD CONSTRAINT ck_feedback_rating CHECK (rating IS NULL OR rating BETWEEN 1 AND 5);
ALTER TABLE public_access_token ADD CONSTRAINT ck_public_token_uses CHECK (used_count >= 0 AND (max_uses IS NULL OR max_uses > 0) AND (max_uses IS NULL OR used_count <= max_uses));
ALTER TABLE data_job ADD CONSTRAINT ck_data_job_progress CHECK (progress BETWEEN 0 AND 100);
ALTER TABLE outbox_event ADD CONSTRAINT ck_outbox_attempts CHECK (attempt_count >= 0);
ALTER TABLE document_sequence ADD CONSTRAINT ck_document_sequence_value CHECK (current_value >= 0);
--rollback SELECT 1;

--changeset esmpf:150-indexes dbms:postgresql
CREATE INDEX idx_business_code ON business(code);
CREATE INDEX idx_customer_name ON customer(business_id,lower(name));
CREATE INDEX idx_customer_interaction_timeline ON customer_interaction(business_id,customer_id,occurred_at DESC,id DESC);
CREATE INDEX idx_service_location_customer ON service_location(business_id,customer_id,status);
CREATE INDEX idx_equipment_customer ON equipment(business_id,customer_id,status);
CREATE INDEX idx_equipment_location ON equipment(business_id,service_location_id,status);
CREATE INDEX idx_equipment_type_lookup ON equipment(business_id,equipment_type_id,status);
CREATE INDEX idx_meter_reading_timeline ON meter_reading(business_id,equipment_id,meter_code,recorded_at DESC,id DESC);
CREATE INDEX idx_maintenance_plan_due ON maintenance_plan(business_id,status,next_due_date);
CREATE INDEX idx_maintenance_occurrence_due ON maintenance_occurrence(business_id,status,due_date);
CREATE INDEX idx_service_request_queue ON service_request(business_id,status,priority,requested_at);
CREATE INDEX idx_service_job_schedule ON service_job(business_id,status,planned_start,id);
CREATE INDEX idx_service_job_customer ON service_job(business_id,customer_id,created_at DESC);
CREATE INDEX idx_service_job_equipment ON service_job(business_id,equipment_id,created_at DESC);
CREATE INDEX idx_service_job_worker ON service_job(business_id,lead_worker_id,status);
CREATE INDEX idx_job_visit_schedule ON job_visit(business_id,status,scheduled_start);
CREATE INDEX idx_job_material_job ON job_material(business_id,job_id);
CREATE INDEX idx_sync_operation_timeline ON sync_operation(business_id,device_id,received_at DESC,id DESC);
CREATE INDEX idx_estimate_job ON estimate(business_id,job_id,created_at DESC);
CREATE INDEX idx_invoice_job ON invoice(business_id,job_id,created_at DESC);
CREATE INDEX idx_invoice_due ON invoice(business_id,status,due_date);
CREATE INDEX idx_payment_invoice ON payment(business_id,invoice_id,created_at DESC);
CREATE INDEX idx_generated_document_source ON generated_document(business_id,source_type,source_id,created_at DESC);
CREATE INDEX idx_attachment_checksum ON attachment(business_id,checksum);
CREATE INDEX idx_notification_claim ON notification(business_id,status,next_attempt_at,created_at,id) WHERE status IN ('QUEUED','FAILED');
CREATE INDEX idx_data_job_queue ON data_job(business_id,status,created_at,id);
CREATE INDEX idx_outbox_claim ON outbox_event(business_id,status,next_attempt_at,created_at,id) WHERE status IN ('PENDING','FAILED');
CREATE INDEX idx_audit_timeline ON audit_log(business_id,occurred_at DESC,id DESC);
CREATE INDEX idx_idempotency_expiry ON idempotency_record(business_id,expires_at);
--rollback SELECT 1;

--changeset esmpf:160-atomic-functions dbms:postgresql splitStatements:false
CREATE OR REPLACE FUNCTION allocate_document_sequence(
    p_business_id UUID,
    p_document_type VARCHAR,
    p_sequence_year INTEGER,
    p_prefix VARCHAR
) RETURNS BIGINT LANGUAGE plpgsql AS $$
DECLARE v_value BIGINT;
BEGIN
    INSERT INTO document_sequence(
        id,business_id,document_type,sequence_year,prefix,current_value,
        created_at,updated_at,version
    ) VALUES (
        gen_random_uuid(),p_business_id,p_document_type,p_sequence_year,p_prefix,1,
        now(),now(),0
    )
    ON CONFLICT (business_id,document_type,sequence_year)
    DO UPDATE SET
        current_value=document_sequence.current_value+1,
        prefix=COALESCE(EXCLUDED.prefix,document_sequence.prefix),
        updated_at=now(),
        version=document_sequence.version+1
    RETURNING current_value INTO v_value;
    RETURN v_value;
END $$;

CREATE OR REPLACE FUNCTION consume_public_access_token(
    p_business_id UUID,
    p_token_id UUID
) RETURNS BOOLEAN LANGUAGE plpgsql AS $$
DECLARE v_updated INTEGER;
BEGIN
    UPDATE public_access_token
       SET used_count=used_count+1,updated_at=now(),version=version+1
     WHERE business_id=p_business_id
       AND id=p_token_id
       AND revoked_at IS NULL
       AND expires_at>now()
       AND (max_uses IS NULL OR used_count<max_uses);
    GET DIAGNOSTICS v_updated = ROW_COUNT;
    RETURN v_updated=1;
END $$;

CREATE OR REPLACE FUNCTION claim_outbox_events(
    p_business_id UUID,
    p_batch_size INTEGER
) RETURNS TABLE(event_id UUID) LANGUAGE plpgsql AS $$
BEGIN
    RETURN QUERY
    WITH candidates AS (
        SELECT id FROM outbox_event
         WHERE business_id=p_business_id
           AND status IN ('PENDING','FAILED')
           AND (next_attempt_at IS NULL OR next_attempt_at<=now())
         ORDER BY created_at,id
         FOR UPDATE SKIP LOCKED
         LIMIT GREATEST(p_batch_size,0)
    )
    UPDATE outbox_event o
       SET status='PUBLISHING',attempt_count=attempt_count+1,
           updated_at=now(),version=version+1
      FROM candidates c
     WHERE o.id=c.id
    RETURNING o.id;
END $$;

CREATE OR REPLACE FUNCTION claim_notifications(
    p_business_id UUID,
    p_batch_size INTEGER
) RETURNS TABLE(notification_id UUID) LANGUAGE plpgsql AS $$
BEGIN
    RETURN QUERY
    WITH candidates AS (
        SELECT id FROM notification
         WHERE business_id=p_business_id
           AND status IN ('QUEUED','FAILED')
           AND (next_attempt_at IS NULL OR next_attempt_at<=now())
         ORDER BY created_at,id
         FOR UPDATE SKIP LOCKED
         LIMIT GREATEST(p_batch_size,0)
    )
    UPDATE notification n
       SET status='SENDING',attempt_count=attempt_count+1,
           updated_at=now(),version=version+1
      FROM candidates c
     WHERE n.id=c.id
    RETURNING n.id;
END $$;
--rollback DROP FUNCTION IF EXISTS claim_notifications(UUID,INTEGER);
--rollback DROP FUNCTION IF EXISTS claim_outbox_events(UUID,INTEGER);
--rollback DROP FUNCTION IF EXISTS consume_public_access_token(UUID,UUID);
--rollback DROP FUNCTION IF EXISTS allocate_document_sequence(UUID,VARCHAR,INTEGER,VARCHAR);
