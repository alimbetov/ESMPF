package com.esmpf.platform.domain;

import com.esmpf.shared.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "public_access_token", indexes = @Index(name = "idx_public_access_token_business", columnList = "business_id"))
class PublicAccessToken extends BaseEntity {
    @Column(name = "purpose") private String purpose;
    @Column(name = "subject_type") private String subjectType;
    @Column(name = "subject_id") private UUID subjectId;
    @Column(name = "token_hash") private String tokenHash;
    @Column(name = "expires_at") private Instant expiresAt;
    @Column(name = "max_uses") private Integer maxUses;
    @Column(name = "used_count") private Integer usedCount;
    @Column(name = "revoked_at") private Instant revokedAt;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "data_job", indexes = @Index(name = "idx_data_job_business", columnList = "business_id"))
class DataJob extends BaseEntity {
    @Column(name = "type") private String type;
    @Column(name = "format") private String format;
    @Column(name = "subject_type") private String subjectType;
    @Column(name = "status") private String status;
    @Column(name = "source_attachment_id") private UUID sourceAttachmentId;
    @Column(name = "result_attachment_id") private UUID resultAttachmentId;
    @Lob @Column(name = "configuration_json") private String configurationJson;
    @Column(name = "progress") private Integer progress;
    @Lob @Column(name = "errors_json") private String errorsJson;
    @Column(name = "completed_at") private Instant completedAt;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "outbox_event", indexes = @Index(name = "idx_outbox_event_business", columnList = "business_id"))
class OutboxEvent extends BaseEntity {
    @Column(name = "aggregate_type") private String aggregateType;
    @Column(name = "aggregate_id") private UUID aggregateId;
    @Column(name = "event_type") private String eventType;
    @Column(name = "event_version") private Integer eventVersion;
    @Lob @Column(name = "payload_json") private String payloadJson;
    @Column(name = "status") private String status;
    @Column(name = "published_at") private Instant publishedAt;
    @Column(name = "attempt_count") private Integer attemptCount;
    @Column(name = "next_attempt_at") private Instant nextAttemptAt;
    @Lob @Column(name = "last_error") private String lastError;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "audit_log", indexes = @Index(name = "idx_audit_log_business", columnList = "business_id"))
class AuditLog extends BaseEntity {
    @Column(name = "actor_type") private String actorType;
    @Column(name = "actor_id") private UUID actorId;
    @Column(name = "action") private String action;
    @Column(name = "subject_type") private String subjectType;
    @Column(name = "subject_id") private UUID subjectId;
    @Lob @Column(name = "before_data_json") private String beforeDataJson;
    @Lob @Column(name = "after_data_json") private String afterDataJson;
    @Lob @Column(name = "metadata_json") private String metadataJson;
    @Column(name = "occurred_at") private Instant occurredAt;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "idempotency_record", indexes = @Index(name = "idx_idempotency_record_business", columnList = "business_id"))
class IdempotencyRecord extends BaseEntity {
    @Column(name = "idempotency_key") private String idempotencyKey;
    @Column(name = "operation") private String operation;
    @Column(name = "request_hash") private String requestHash;
    @Column(name = "response_reference") private String responseReference;
    @Column(name = "status") private String status;
    @Column(name = "expires_at") private Instant expiresAt;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "integration_connection", indexes = @Index(name = "idx_integration_connection_business", columnList = "business_id"))
class IntegrationConnection extends BaseEntity {
    @Column(name = "type") private String type;
    @Column(name = "name") private String name;
    @Column(name = "status") private String status;
    @Lob @Column(name = "configuration_json") private String configurationJson;
    @Column(name = "secret_reference") private String secretReference;
    @Column(name = "last_successful_at") private Instant lastSuccessfulAt;
    @Column(name = "last_error_at") private Instant lastErrorAt;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "document_sequence", indexes = @Index(name = "idx_document_sequence_business", columnList = "business_id"))
class DocumentSequence extends BaseEntity {
    @Column(name = "document_type") private String documentType;
    @Column(name = "sequence_year") private Integer sequenceYear;
    @Column(name = "prefix") private String prefix;
    @Column(name = "current_value") private Long currentValue;
}
