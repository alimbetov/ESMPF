package com.esmpf.customer.domain;

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
@Table(name = "customer", indexes = @Index(name = "idx_customer_business", columnList = "business_id"))
class Customer extends TenantEntity {
    @Column(name = "type", nullable = false, length = 40) private String type;
    @Column(name = "name", nullable = false, length = 200) private String name;
    @Column(name = "primary_phone", length = 50) private String primaryPhone;
    @Column(name = "primary_email", length = 320) private String primaryEmail;
    @Column(name = "preferred_language", length = 10) private String preferredLanguage;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "contacts_json", columnDefinition = "jsonb") private String contactsJson;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "notification_preferences_json", columnDefinition = "jsonb") private String notificationPreferencesJson;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "billing_data_json", columnDefinition = "jsonb") private String billingDataJson;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "consents_json", columnDefinition = "jsonb") private String consentsJson;
    @Column(name = "status", nullable = false, length = 40) private String status;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "customer_interaction", indexes = @Index(name = "idx_customer_interaction_business", columnList = "business_id"))
class CustomerInteraction extends TenantEntity {
    @Column(name = "customer_id", nullable = false) private UUID customerId;
    @Column(name = "type", nullable = false, length = 60) private String type;
    @Column(name = "subject", length = 300) private String subject;
    @Column(name = "content", columnDefinition = "text") private String content;
    @Column(name = "occurred_at", nullable = false) private Instant occurredAt;
    @Column(name = "created_by") private UUID createdBy;
    @Column(name = "related_subject_type", length = 80) private String relatedSubjectType;
    @Column(name = "related_subject_id") private UUID relatedSubjectId;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "service_location", indexes = @Index(name = "idx_service_location_business", columnList = "business_id"))
class ServiceLocation extends TenantEntity {
    @Column(name = "customer_id", nullable = false) private UUID customerId;
    @Column(name = "parent_location_id") private UUID parentLocationId;
    @Column(name = "name", nullable = false, length = 200) private String name;
    @Column(name = "type", length = 60) private String type;
    @Column(name = "address", length = 500) private String address;
    @Column(name = "latitude") private Double latitude;
    @Column(name = "longitude") private Double longitude;
    @Column(name = "timezone", length = 100) private String timezone;
    @Column(name = "access_instructions", length = 1000) private String accessInstructions;
    @Column(name = "status", nullable = false, length = 40) private String status;
}
