package com.esmpf.communication.domain;

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
@Table(name = "notification_template", indexes = @Index(name = "idx_notification_template_business", columnList = "business_id"))
class NotificationTemplate extends TenantEntity {
    @Column(name = "code", nullable = false, length = 100) private String code;
    @Column(name = "channel", nullable = false, length = 40) private String channel;
    @Column(name = "locale", nullable = false, length = 20) private String locale;
    @Column(name = "template_version", nullable = false) private Integer templateVersion;
    @Column(name = "subject_template", length = 500) private String subjectTemplate;
    @Column(name = "body_template", nullable = false, columnDefinition = "text") private String bodyTemplate;
    @Column(name = "status", nullable = false, length = 40) private String status;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "notification", indexes = @Index(name = "idx_notification_business", columnList = "business_id"))
class Notification extends TenantEntity {
    @Column(name = "customer_id") private UUID customerId;
    @Column(name = "recipient", nullable = false, length = 500) private String recipient;
    @Column(name = "channel", nullable = false, length = 40) private String channel;
    @Column(name = "notification_template_id") private UUID notificationTemplateId;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "payload_json", nullable = false, columnDefinition = "jsonb") private String payloadJson;
    @Column(name = "status", nullable = false, length = 40) private String status;
    @Column(name = "attempt_count", nullable = false) private Integer attemptCount;
    @Column(name = "next_attempt_at") private Instant nextAttemptAt;
    @Column(name = "sent_at") private Instant sentAt;
    @Column(name = "provider_message_id", length = 255) private String providerMessageId;
    @Column(name = "last_error", columnDefinition = "text") private String lastError;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "customer_feedback", indexes = @Index(name = "idx_customer_feedback_business", columnList = "business_id"))
class CustomerFeedback extends TenantEntity {
    @Column(name = "customer_id", nullable = false) private UUID customerId;
    @Column(name = "job_id") private UUID jobId;
    @Column(name = "type", nullable = false, length = 60) private String type;
    @Column(name = "rating") private Integer rating;
    @Column(name = "comment", columnDefinition = "text") private String comment;
    @Column(name = "publication_consent", nullable = false) private Boolean publicationConsent;
    @Column(name = "status", nullable = false, length = 40) private String status;
    @Column(name = "company_response", columnDefinition = "text") private String companyResponse;
    @Column(name = "responded_by") private UUID respondedBy;
    @Column(name = "responded_at") private Instant respondedAt;
    @Column(name = "resolved_at") private Instant resolvedAt;
}
