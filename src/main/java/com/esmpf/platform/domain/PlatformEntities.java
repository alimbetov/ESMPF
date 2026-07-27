package com.esmpf.platform.domain;

import com.esmpf.shared.persistence.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "public_access_token", indexes = @Index(name = "idx_public_access_token_business", columnList = "business_id"))
class PublicAccessToken extends TenantEntity {
    @Column(name = "purpose", nullable = false, length = 80) private String purpose;
    @Column(name = "subject_type", nullable = false, length = 80) private String subjectType;
    @Column(name = "subject_id", nullable = false) private UUID subjectId;
    @Column(name = "token_hash", nullable = false, length = 255) private String tokenHash;
    @Column(name = "expires_at", nullable = false) private Instant expiresAt;
    @Column(name = "max_uses") private Integer maxUses;
    @Column(name = "used_count", nullable = false) private Integer usedCount;
    @Column(name = "revoked_at") private Instant revokedAt;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "data_job", indexes = @Index(name = "idx_data_job_business", columnList = "business_id"))
class DataJob extends TenantEntity {
    @Column(name = "type", nullable = false, length = 80) private String type;
    @Column(name = "format", nullable = false, length = 40) private String format;
    @Column(name = "subject_type", length = 80) private String subjectType;
    @Column(name = "status", nullable = false, length = 40) private String status;
    @Column(name = "source_attachment_id") private UUID sourceAttachmentId;
    @Column(name = "result_attachment_id") private UUID resultAttachmentId;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "configuration_json", columnDefinition = "jsonb") private String configurationJson;
    @Column(name = "progress", nullable = false) private Integer progress;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "errors_json", columnDefinition = "jsonb") private String errorsJson;
    @Column(name = "completed_at") private Instant completedAt;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "outbox_event", indexes = @Index(name = "idx_outbox_event_business", columnList = "business_id"))
class OutboxEvent extends TenantEntity {
    @Column(name = "aggregate_type", nullable = false, length = 100) private String aggregateType;
    @Column(name = "aggregate_id", nullable = false) private UUID aggregateId;
    @Column(name = "event_type", nullable = false, length = 150) private String eventType;
    @Column(name = "event_version", nullable = false) private Integer eventVersion;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "payload_json", nullable = false, columnDefinition = "jsonb") private String payloadJson;
    @Column(name = "status", nullable = false, length = 40) private String status;
    @Column(name = "published_at") private Instant publishedAt;
    @Column(name = "attempt_count", nullable = false) private Integer attemptCount;
    @Column(name = "next_attempt_at") private Instant nextAttemptAt;
    @Column(name = "last_error", columnDefinition = "text") private String lastError;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "audit_log", indexes = @Index(name = "idx_audit_log_business", columnList = "business_id"))
class AuditLog extends TenantEntity {
    @Column(name = "actor_type", nullable = false, length = 80) private String actorType;
    @Column(name = "actor_id") private UUID actorId;
    @Column(name = "action", nullable = false, length = 150) private String action;
    @Column(name = "subject_type", nullable = false, length = 100) private String subjectType;
    @Column(name = "subject_id", nullable = false) private UUID subjectId;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "before_data_json", columnDefinition = "jsonb") private String beforeDataJson;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "after_data_json", columnDefinition = "jsonb") private String afterDataJson;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "metadata_json", columnDefinition = "jsonb") private String metadataJson;
    @Column(name = "occurred_at", nullable = false) private Instant occurredAt;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "idempotency_record", indexes = @Index(name = "idx_idempotency_record_business", columnList = "business_id"))
class IdempotencyRecord extends TenantEntity {
    @Column(name = "idempotency_key", nullable = false, length = 200) private String idempotencyKey;
    @Column(name = "operation", nullable = false, length = 150) private String operation;
    @Column(name = "request_hash", nullable = false, length = 128) private String requestHash;
    @Column(name = "response_reference", length = 500) private String responseReference;
    @Column(name = "status", nullable = false, length = 40) private String status;
    @Column(name = "expires_at", nullable = false) private Instant expiresAt;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "integration_connection", indexes = @Index(name = "idx_integration_connection_business", columnList = "business_id"))
class IntegrationConnection extends TenantEntity {
    @Column(name = "type", nullable = false, length = 80) private String type;
    @Column(name = "name", nullable = false, length = 200) private String name;
    @Column(name = "status", nullable = false, length = 40) private String status;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "configuration_json", columnDefinition = "jsonb") private String configurationJson;
    @Column(name = "secret_reference", length = 500) private String secretReference;
    @Column(name = "last_successful_at") private Instant lastSuccessfulAt;
    @Column(name = "last_error_at") private Instant lastErrorAt;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "document_sequence", indexes = @Index(name = "idx_document_sequence_business", columnList = "business_id"))
class DocumentSequence extends TenantEntity {
    @Column(name = "document_type", nullable = false, length = 80) private String documentType;
    @Column(name = "sequence_year", nullable = false) private Integer sequenceYear;
    @Column(name = "prefix", length = 40) private String prefix;
    @Column(name = "current_value", nullable = false) private Long currentValue;
}
