package com.esmpf.identity;

import static com.esmpf.identity.RbacDtos.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/access")
@RequiredArgsConstructor
public class RbacRestController {
    private final RbacService service;

    @GetMapping("/permissions")
    public List<PermissionResponse> listPermissions() { return service.listPermissions(); }

    @PostMapping("/roles") @ResponseStatus(HttpStatus.CREATED)
    public RoleResponse createRole(@Valid @RequestBody RoleCommand command) { return service.createRole(command); }

    @GetMapping("/roles/{roleId}")
    public RoleResponse getRole(@PathVariable UUID roleId) { return service.getRole(roleId); }

    @GetMapping("/roles")
    public Page<RoleResponse> listRoles(Pageable pageable) { return service.listRoles(pageable); }

    @PutMapping("/roles/{roleId}")
    public RoleResponse updateRole(@PathVariable UUID roleId, @Valid @RequestBody RoleCommand command) {
        return service.updateRole(roleId, command);
    }

    @PostMapping("/roles/{roleId}/actions/activate")
    public RoleResponse activateRole(@PathVariable UUID roleId, @Valid @RequestBody VersionCommand command) {
        return service.activateRole(roleId, command.version());
    }

    @PostMapping("/roles/{roleId}/actions/deactivate")
    public RoleResponse deactivateRole(@PathVariable UUID roleId, @Valid @RequestBody VersionCommand command) {
        return service.deactivateRole(roleId, command.version());
    }

    @GetMapping("/roles/{roleId}/permissions")
    public RoleResponse getRolePermissions(@PathVariable UUID roleId) { return service.getRole(roleId); }

    @PutMapping("/roles/{roleId}/permissions")
    public RoleResponse replacePermissions(@PathVariable UUID roleId,
                                           @Valid @RequestBody ReplaceRolePermissionsCommand command) {
        return service.replacePermissions(roleId, command);
    }

    @PostMapping("/users/{userId}/role-assignments") @ResponseStatus(HttpStatus.CREATED)
    public RoleAssignmentResponse assignRole(@PathVariable UUID userId,
                                             @Valid @RequestBody RoleAssignmentCommand command) {
        return service.assignRole(userId, command);
    }

    @GetMapping("/users/{userId}/role-assignments")
    public Page<RoleAssignmentResponse> listAssignments(@PathVariable UUID userId, Pageable pageable) {
        return service.listAssignments(userId, pageable);
    }

    @PostMapping("/role-assignments/{assignmentId}/actions/revoke")
    public RoleAssignmentResponse revokeAssignment(@PathVariable UUID assignmentId,
                                                   @Valid @RequestBody VersionCommand command) {
        return service.revokeAssignment(assignmentId, command.version());
    }
}
