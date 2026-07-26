package com.esmpf.identity.domain;

import com.esmpf.shared.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "business", indexes = @Index(name = "idx_business_business", columnList = "business_id"))
class Business extends BaseEntity {
    @Column(name = "name") private String name;
    @Column(name = "code") private String code;
    @Column(name = "timezone") private String timezone;
    @Column(name = "default_language") private String defaultLanguage;
    @Column(name = "currency") private String currency;
    @Column(name = "status") private String status;
    @Lob @Column(name = "settings_json") private String settingsJson;
}

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "business_location", indexes = @Index(name = "idx_business_location_business", columnList = "business_id"))
class BusinessLocation extends BaseEntity {
    @Column(name = "name") private String name;
    @Column(name = "address") private String address;
    @Column(name = "latitude") private Double latitude;
    @Column(name = "longitude") private Double longitude;
    @Column(name = "timezone") private String timezone;
    @Column(name = "active") private Boolean active;
}

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "user_account", indexes = @Index(name = "idx_user_account_business", columnList = "business_id"))
class UserAccount extends BaseEntity {
    @Column(name = "email") private String email;
    @Column(name = "phone") private String phone;
    @Column(name = "password_hash") private String passwordHash;
    @Column(name = "full_name") private String fullName;
    @Column(name = "role") private String role;
    @Column(name = "worker") private Boolean worker;
    @Column(name = "active") private Boolean active;
    @Column(name = "external_provider") private String externalProvider;
    @Column(name = "external_subject") private String externalSubject;
}

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "worker_qualification", indexes = @Index(name = "idx_worker_qualification_business", columnList = "business_id"))
class WorkerQualification extends BaseEntity {
    @Column(name = "user_id") private UUID userId;
    @Column(name = "type") private String type;
    @Column(name = "name") private String name;
    @Column(name = "issuer") private String issuer;
    @Column(name = "reference_number") private String referenceNumber;
    @Column(name = "valid_from") private LocalDate validFrom;
    @Column(name = "valid_until") private LocalDate validUntil;
    @Column(name = "attachment_id") private UUID attachmentId;
    @Column(name = "status") private String status;
}
