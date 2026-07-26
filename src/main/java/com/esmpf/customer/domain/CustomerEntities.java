package com.esmpf.customer.domain;

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
@Table(name = "customer", indexes = @Index(name = "idx_customer_business", columnList = "business_id"))
class Customer extends BaseEntity {
    @Column(name = "type") private String type;
    @Column(name = "name") private String name;
    @Column(name = "primary_phone") private String primaryPhone;
    @Column(name = "primary_email") private String primaryEmail;
    @Column(name = "preferred_language") private String preferredLanguage;
    @Lob @Column(name = "contacts_json") private String contactsJson;
    @Lob @Column(name = "notification_preferences_json") private String notificationPreferencesJson;
    @Lob @Column(name = "billing_data_json") private String billingDataJson;
    @Lob @Column(name = "consents_json") private String consentsJson;
    @Column(name = "status") private String status;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "customer_interaction", indexes = @Index(name = "idx_customer_interaction_business", columnList = "business_id"))
class CustomerInteraction extends BaseEntity {
    @Column(name = "customer_id") private UUID customerId;
    @Column(name = "type") private String type;
    @Column(name = "subject") private String subject;
    @Lob @Column(name = "content") private String content;
    @Column(name = "occurred_at") private Instant occurredAt;
    @Column(name = "created_by") private UUID createdBy;
    @Column(name = "related_subject_type") private String relatedSubjectType;
    @Column(name = "related_subject_id") private UUID relatedSubjectId;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "service_location", indexes = @Index(name = "idx_service_location_business", columnList = "business_id"))
class ServiceLocation extends BaseEntity {
    @Column(name = "customer_id") private UUID customerId;
    @Column(name = "parent_location_id") private UUID parentLocationId;
    @Column(name = "name") private String name;
    @Column(name = "type") private String type;
    @Column(name = "address") private String address;
    @Column(name = "latitude") private Double latitude;
    @Column(name = "longitude") private Double longitude;
    @Column(name = "timezone") private String timezone;
    @Column(name = "access_instructions") private String accessInstructions;
    @Column(name = "status") private String status;
}
