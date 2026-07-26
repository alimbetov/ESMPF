package com.esmpf.communication.domain;

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
@Table(name = "notification_template", indexes = @Index(name = "idx_notification_template_business", columnList = "business_id"))
class NotificationTemplate extends BaseEntity {
    @Column(name = "code") private String code;
    @Column(name = "channel") private String channel;
    @Column(name = "locale") private String locale;
    @Column(name = "template_version") private Integer templateVersion;
    @Column(name = "subject_template") private String subjectTemplate;
    @Lob @Column(name = "body_template") private String bodyTemplate;
    @Column(name = "status") private String status;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "notification", indexes = @Index(name = "idx_notification_business", columnList = "business_id"))
class Notification extends BaseEntity {
    @Column(name = "customer_id") private UUID customerId;
    @Column(name = "recipient") private String recipient;
    @Column(name = "channel") private String channel;
    @Column(name = "notification_template_id") private UUID notificationTemplateId;
    @Lob @Column(name = "payload_json") private String payloadJson;
    @Column(name = "status") private String status;
    @Column(name = "attempt_count") private Integer attemptCount;
    @Column(name = "next_attempt_at") private Instant nextAttemptAt;
    @Column(name = "sent_at") private Instant sentAt;
    @Column(name = "provider_message_id") private String providerMessageId;
    @Lob @Column(name = "last_error") private String lastError;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "customer_feedback", indexes = @Index(name = "idx_customer_feedback_business", columnList = "business_id"))
class CustomerFeedback extends BaseEntity {
    @Column(name = "customer_id") private UUID customerId;
    @Column(name = "job_id") private UUID jobId;
    @Column(name = "type") private String type;
    @Column(name = "rating") private Integer rating;
    @Lob @Column(name = "comment") private String comment;
    @Column(name = "publication_consent") private Boolean publicationConsent;
    @Column(name = "status") private String status;
    @Lob @Column(name = "company_response") private String companyResponse;
    @Column(name = "responded_by") private UUID respondedBy;
    @Column(name = "responded_at") private Instant respondedAt;
    @Column(name = "resolved_at") private Instant resolvedAt;
}
