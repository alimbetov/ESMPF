package com.esmpf.identity;

import static com.esmpf.identity.RbacDtos.*;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RbacService {
    List<PermissionResponse> listPermissions();
    RoleResponse createRole(RoleCommand command);
    RoleResponse getRole(UUID roleId);
    Page<RoleResponse> listRoles(Pageable pageable);
    RoleResponse updateRole(UUID roleId, RoleCommand command);
    RoleResponse activateRole(UUID roleId, long version);
    RoleResponse deactivateRole(UUID roleId, long version);
    RoleResponse replacePermissions(UUID roleId, ReplaceRolePermissionsCommand command);
    RoleAssignmentResponse assignRole(UUID userId, RoleAssignmentCommand command);
    Page<RoleAssignmentResponse> listAssignments(UUID userId, Pageable pageable);
    RoleAssignmentResponse revokeAssignment(UUID assignmentId, long version);
}
