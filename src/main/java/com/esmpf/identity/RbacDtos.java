package com.esmpf.identity;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public final class RbacDtos {
    private RbacDtos() {}

    public record RoleCommand(long version, String code, String name, String description) {}
    public record ReplaceRolePermissionsCommand(long version, Set<PermissionCode> permissions) {}
    public record RoleAssignmentCommand(UUID roleId, Instant validFrom, Instant validUntil) {}
    public record VersionCommand(long version) {}

    public record PermissionResponse(PermissionCode code, String category, String description) {}
    public record RoleResponse(UUID id, long version, String code, String name, String description,
                               boolean system, boolean active, Set<PermissionCode> permissions,
                               Instant createdAt, Instant updatedAt) {}
    public record RoleAssignmentResponse(UUID id, long version, UUID userId, UUID roleId,
                                         String roleCode, String status, Instant validFrom,
                                         Instant validUntil, UUID assignedBy, Instant assignedAt,
                                         UUID revokedBy, Instant revokedAt) {}
    public record EffectiveAccess(UUID userId, UUID businessId, Set<String> roleCodes,
                                  Set<PermissionCode> permissions) {}
}
