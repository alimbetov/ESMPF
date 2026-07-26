package com.esmpf.identity.domain;

import com.esmpf.shared.persistence.BaseEntity;
import com.esmpf.shared.persistence.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "business", indexes = @Index(name = "idx_business_code", columnList = "code"))
class Business extends BaseEntity {
    @Column(name = "name", nullable = false, length = 200) private String name;
    @Column(name = "code", nullable = false, length = 100, unique = true) private String code;
    @Column(name = "timezone", nullable = false, length = 100) private String timezone;
    @Column(name = "default_language", nullable = false, length = 10) private String defaultLanguage;
    @Column(name = "currency", nullable = false, length = 3) private String currency;
    @Column(name = "status", nullable = false, length = 40) private String status;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "settings_json", columnDefinition = "jsonb") private String settingsJson;
}

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "business_location", indexes = @Index(name = "idx_business_location_business", columnList = "business_id"))
class BusinessLocation extends TenantEntity {
    @Column(name = "name", nullable = false, length = 200) private String name;
    @Column(name = "address", length = 500) private String address;
    @Column(name = "latitude") private Double latitude;
    @Column(name = "longitude") private Double longitude;
    @Column(name = "timezone", length = 100) private String timezone;
    @Column(name = "active", nullable = false) private Boolean active;
}

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "user_account", indexes = @Index(name = "idx_user_account_business", columnList = "business_id"))
class UserAccount extends TenantEntity {
    @Column(name = "email", length = 320) private String email;
    @Column(name = "phone", length = 50) private String phone;
    @Column(name = "password_hash", length = 255) private String passwordHash;
    @Column(name = "full_name", nullable = false, length = 200) private String fullName;
    @Column(name = "role", nullable = false, length = 60) private String role;
    @Column(name = "worker", nullable = false) private Boolean worker;
    @Column(name = "active", nullable = false) private Boolean active;
    @Column(name = "external_provider", length = 100) private String externalProvider;
    @Column(name = "external_subject", length = 255) private String externalSubject;
}

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "worker_qualification", indexes = @Index(name = "idx_worker_qualification_business", columnList = "business_id"))
class WorkerQualification extends TenantEntity {
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "type", nullable = false, length = 80) private String type;
    @Column(name = "name", nullable = false, length = 200) private String name;
    @Column(name = "issuer", length = 200) private String issuer;
    @Column(name = "reference_number", length = 100) private String referenceNumber;
    @Column(name = "valid_from") private LocalDate validFrom;
    @Column(name = "valid_until") private LocalDate validUntil;
    @Column(name = "attachment_id") private UUID attachmentId;
    @Column(name = "status", nullable = false, length = 40) private String status;
}
