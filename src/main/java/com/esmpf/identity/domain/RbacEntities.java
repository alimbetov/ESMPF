package com.esmpf.identity.domain;

import com.esmpf.shared.persistence.BaseEntity;
import com.esmpf.shared.persistence.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @Entity @Table(name = "permission")
class PermissionEntity {
    @Id @Column(name = "code", length = 100) private String code;
    @Column(name = "category", nullable = false, length = 50) private String category;
    @Column(name = "description", nullable = false, length = 300) private String description;
}

@Getter @Setter @NoArgsConstructor @Entity @Table(name = "role_permission")
class RolePermission extends BaseEntity {
    @Column(name = "role_id", nullable = false) private UUID roleId;
    @Column(name = "permission_code", nullable = false, length = 100) private String permissionCode;
    @Column(name = "granted_by") private UUID grantedBy;
    @Column(name = "granted_at", nullable = false) private Instant grantedAt;
}

@Getter @Setter @NoArgsConstructor @Entity @Table(name = "user_role_assignment")
class UserRoleAssignment extends TenantEntity {
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "role_id", nullable = false) private UUID roleId;
    @Column(name = "status", nullable = false, length = 20) private String status;
    @Column(name = "valid_from") private Instant validFrom;
    @Column(name = "valid_until") private Instant validUntil;
    @Column(name = "assigned_by", nullable = false) private UUID assignedBy;
    @Column(name = "assigned_at", nullable = false) private Instant assignedAt;
    @Column(name = "revoked_by") private UUID revokedBy;
    @Column(name = "revoked_at") private Instant revokedAt;
}
