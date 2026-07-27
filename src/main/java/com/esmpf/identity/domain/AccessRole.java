package com.esmpf.identity.domain;

import com.esmpf.shared.persistence.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "access_role",
        indexes = {
                @Index(name = "idx_access_role_business", columnList = "business_id"),
                @Index(name = "idx_access_role_business_active", columnList = "business_id,active")
        },
        uniqueConstraints = @UniqueConstraint(
                name = "uk_access_role_business_code_exact",
                columnNames = {"business_id", "code"})
)
class AccessRole extends TenantEntity {

    @Column(name = "code", nullable = false, length = 80, updatable = false)
    private String code;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "system", nullable = false, updatable = false)
    private boolean system;

    @Column(name = "active", nullable = false)
    private boolean active;
}
