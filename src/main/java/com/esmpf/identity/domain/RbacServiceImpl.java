package com.esmpf.identity.domain;

import static com.esmpf.identity.RbacDtos.*;

import com.esmpf.identity.AccessControlQuery;
import com.esmpf.identity.PermissionCode;
import com.esmpf.identity.RbacService;
import com.esmpf.shared.exception.EntityNotFoundException;
import com.esmpf.shared.exception.StaleEntityException;
import com.esmpf.shared.tenant.TenantContext;
import java.time.Instant;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class RbacServiceImpl implements RbacService, AccessControlQuery {
    private static final String ACTIVE = "ACTIVE";
    private static final String REVOKED = "REVOKED";
    private static final String OWNER = "OWNER";

    private final TenantContext tenantContext;
    private final BusinessRepository businessRepository;
    private final UserAccountRepository userRepository;
    private final AccessRoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRoleAssignmentRepository assignmentRepository;

    @Override @Transactional(readOnly = true)
    public List<PermissionResponse> listPermissions() {
        return permissionRepository.findAll().stream()
                .map(p -> new PermissionResponse(PermissionCode.valueOf(p.getCode()), p.getCategory(), p.getDescription()))
                .sorted(java.util.Comparator.comparing(p -> p.code().name()))
                .toList();
    }

    @Override @Transactional
    public RoleResponse createRole(RoleCommand command) {
        String code = normalizeCode(command.code());
        if (roleRepository.findByBusinessIdAndCodeIgnoreCase(tenant(), code).isPresent()) {
            throw new IllegalArgumentException("Role code already exists in tenant");
        }
        AccessRole role = new AccessRole();
        role.setBusinessId(tenant());
        role.setCode(code);
        role.setName(requireText(command.name(), "name", 150));
        role.setDescription(trim(command.description(), 500));
        role.setSystem(false);
        role.setActive(true);
        return response(roleRepository.saveAndFlush(role));
    }

    @Override @Transactional(readOnly = true)
    public RoleResponse getRole(UUID roleId) { return response(requireRole(roleId)); }

    @Override @Transactional(readOnly = true)
    public Page<RoleResponse> listRoles(Pageable pageable) {
        return roleRepository.findAllByBusinessId(tenant(), pageable).map(this::response);
    }

    @Override @Transactional
    public RoleResponse updateRole(UUID roleId, RoleCommand command) {
        AccessRole role = requireRole(roleId);
        checkVersion("AccessRole", roleId, command.version(), role.getVersion());
        if (role.isSystem() && command.code() != null && !role.getCode().equals(normalizeCode(command.code()))) {
            throw new IllegalStateException("System role code is immutable");
        }
        if (!role.isSystem() && command.code() != null) {
            String code = normalizeCode(command.code());
            roleRepository.findByBusinessIdAndCodeIgnoreCase(tenant(), code)
                    .filter(existing -> !existing.getId().equals(roleId))
                    .ifPresent(existing -> { throw new IllegalArgumentException("Role code already exists in tenant"); });
            role.setCode(code);
        }
        if (command.name() != null) role.setName(requireText(command.name(), "name", 150));
        role.setDescription(trim(command.description(), 500));
        return response(roleRepository.saveAndFlush(role));
    }

    @Override @Transactional
    public RoleResponse activateRole(UUID roleId, long version) { return setRoleActive(roleId, version, true); }

    @Override @Transactional
    public RoleResponse deactivateRole(UUID roleId, long version) {
        AccessRole role = requireRole(roleId);
        if (OWNER.equals(role.getCode())) throw new IllegalStateException("OWNER role cannot be deactivated");
        return setRoleActive(roleId, version, false);
    }

    @Override @Transactional
    public RoleResponse replacePermissions(UUID roleId, ReplaceRolePermissionsCommand command) {
        AccessRole role = requireRole(roleId);
        checkVersion("AccessRole", roleId, command.version(), role.getVersion());
        Set<PermissionCode> requested = command.permissions() == null
                ? Set.of() : Set.copyOf(command.permissions());
        if (OWNER.equals(role.getCode()) && requested.size() != PermissionCode.values().length) {
            throw new IllegalStateException("OWNER must retain the complete permission catalogue");
        }
        rolePermissionRepository.deleteAllByRoleId(roleId);
        Instant now = Instant.now();
        UUID actor = tenantContext.requireUserId();
        requested.forEach(code -> {
            if (!permissionRepository.existsById(code.name())) {
                throw new IllegalArgumentException("Unknown permission: " + code);
            }
            RolePermission link = new RolePermission();
            link.setRoleId(roleId);
            link.setPermissionCode(code.name());
            link.setGrantedBy(actor);
            link.setGrantedAt(now);
            rolePermissionRepository.save(link);
        });
        roleRepository.saveAndFlush(role);
        return response(role);
    }

    @Override @Transactional
    public RoleAssignmentResponse assignRole(UUID userId, RoleAssignmentCommand command) {
        UserAccount user = userRepository.findByIdAndBusinessId(userId, tenant())
                .orElseThrow(() -> new EntityNotFoundException("UserAccount", userId));
        if (!Boolean.TRUE.equals(user.getActive())) throw new IllegalStateException("Inactive user cannot receive roles");
        AccessRole role = requireRole(command.roleId());
        if (!role.isActive()) throw new IllegalStateException("Inactive role cannot be assigned");
        if (command.validFrom() != null && command.validUntil() != null && !command.validUntil().isAfter(command.validFrom())) {
            throw new IllegalArgumentException("validUntil must be after validFrom");
        }
        if (assignmentRepository.existsByBusinessIdAndUserIdAndRoleIdAndStatus(tenant(), userId, role.getId(), ACTIVE)) {
            throw new IllegalArgumentException("Active role assignment already exists");
        }
        UserRoleAssignment assignment = new UserRoleAssignment();
        assignment.setBusinessId(tenant());
        assignment.setUserId(userId);
        assignment.setRoleId(role.getId());
        assignment.setStatus(ACTIVE);
        assignment.setValidFrom(command.validFrom());
        assignment.setValidUntil(command.validUntil());
        assignment.setAssignedBy(tenantContext.requireUserId());
        assignment.setAssignedAt(Instant.now());
        return assignmentResponse(assignmentRepository.saveAndFlush(assignment), role.getCode());
    }

    @Override @Transactional(readOnly = true)
    public Page<RoleAssignmentResponse> listAssignments(UUID userId, Pageable pageable) {
        userRepository.findByIdAndBusinessId(userId, tenant())
                .orElseThrow(() -> new EntityNotFoundException("UserAccount", userId));
        return assignmentRepository.findAllByBusinessIdAndUserId(tenant(), userId, pageable)
                .map(a -> assignmentResponse(a, roleRepository.findByIdAndBusinessId(a.getRoleId(), tenant())
                        .map(AccessRole::getCode).orElse("UNKNOWN")));
    }

    @Override @Transactional
    public RoleAssignmentResponse revokeAssignment(UUID assignmentId, long version) {
        UserRoleAssignment assignment = requireAssignment(assignmentId);
        checkVersion("UserRoleAssignment", assignmentId, version, assignment.getVersion());
        if (!ACTIVE.equals(assignment.getStatus())) throw new IllegalStateException("Role assignment is not active");
        AccessRole role = requireRole(assignment.getRoleId());
        if (OWNER.equals(role.getCode())) {
            businessRepository.lockById(tenant()).orElseThrow(() -> new EntityNotFoundException("Business", tenant()));
            long owners = assignmentRepository.countEffectiveRoleAssignments(tenant(), role.getId(), Instant.now());
            if (owners <= 1) throw new IllegalStateException("Last active OWNER assignment cannot be revoked");
        }
        assignment.setStatus(REVOKED);
        assignment.setRevokedBy(tenantContext.requireUserId());
        assignment.setRevokedAt(Instant.now());
        return assignmentResponse(assignmentRepository.saveAndFlush(assignment), role.getCode());
    }

    @Override @Transactional(readOnly = true)
    public EffectiveAccess resolveEffectiveAccess(UUID userId) {
        UserAccount user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("UserAccount", userId));
        if (!Boolean.TRUE.equals(user.getActive())) throw new IllegalStateException("User account is inactive");
        Business business = businessRepository.findById(user.getBusinessId())
                .orElseThrow(() -> new EntityNotFoundException("Business", user.getBusinessId()));
        if (!"ACTIVE".equals(business.getStatus())) throw new IllegalStateException("Business is not active");

        Set<String> roles = new LinkedHashSet<>();
        EnumSet<PermissionCode> permissions = EnumSet.noneOf(PermissionCode.class);
        for (UserRoleAssignment assignment : assignmentRepository.findEffective(user.getBusinessId(), userId, Instant.now())) {
            roleRepository.findByIdAndBusinessId(assignment.getRoleId(), user.getBusinessId())
                    .filter(AccessRole::isActive)
                    .ifPresent(role -> {
                        roles.add(role.getCode());
                        rolePermissionRepository.findAllByRoleId(role.getId()).forEach(link ->
                                permissions.add(PermissionCode.valueOf(link.getPermissionCode())));
                    });
        }
        return new EffectiveAccess(userId, user.getBusinessId(), Set.copyOf(roles), Set.copyOf(permissions));
    }

    @Override @Transactional(readOnly = true)
    public boolean hasPermission(UUID userId, PermissionCode permission) {
        return resolveEffectiveAccess(userId).permissions().contains(permission);
    }

    private RoleResponse setRoleActive(UUID id, long version, boolean active) {
        AccessRole role = requireRole(id);
        checkVersion("AccessRole", id, version, role.getVersion());
        if (role.isActive() == active) throw new IllegalStateException(active ? "Role already active" : "Role already inactive");
        role.setActive(active);
        return response(roleRepository.saveAndFlush(role));
    }

    private RoleResponse response(AccessRole role) {
        Set<PermissionCode> permissions = rolePermissionRepository.findAllByRoleId(role.getId()).stream()
                .map(RolePermission::getPermissionCode).map(PermissionCode::valueOf)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        return new RoleResponse(role.getId(), role.getVersion(), role.getCode(), role.getName(),
                role.getDescription(), role.isSystem(), role.isActive(), permissions,
                role.getCreatedAt(), role.getUpdatedAt());
    }

    private RoleAssignmentResponse assignmentResponse(UserRoleAssignment a, String roleCode) {
        return new RoleAssignmentResponse(a.getId(), a.getVersion(), a.getUserId(), a.getRoleId(), roleCode,
                a.getStatus(), a.getValidFrom(), a.getValidUntil(), a.getAssignedBy(), a.getAssignedAt(),
                a.getRevokedBy(), a.getRevokedAt());
    }

    private AccessRole requireRole(UUID id) {
        return roleRepository.findByIdAndBusinessId(id, tenant())
                .orElseThrow(() -> new EntityNotFoundException("AccessRole", id));
    }
    private UserRoleAssignment requireAssignment(UUID id) {
        return assignmentRepository.findByIdAndBusinessId(id, tenant())
                .orElseThrow(() -> new EntityNotFoundException("UserRoleAssignment", id));
    }
    private UUID tenant() { return tenantContext.requireBusinessId(); }
    private static String normalizeCode(String value) {
        String code = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        if (!code.matches("[A-Z][A-Z0-9_]{1,79}")) throw new IllegalArgumentException("Invalid role code");
        return code;
    }
    private static String requireText(String value, String field, int max) {
        String result = value == null ? "" : value.trim();
        if (result.isEmpty() || result.length() > max) throw new IllegalArgumentException(field + " is required and limited to " + max);
        return result;
    }
    private static String trim(String value, int max) {
        if (value == null) return null;
        String result = value.trim();
        if (result.length() > max) throw new IllegalArgumentException("Text exceeds " + max + " characters");
        return result.isEmpty() ? null : result;
    }
    private static void checkVersion(String type, UUID id, long expected, long actual) {
        if (expected != actual) throw new StaleEntityException(type, id, expected, actual);
    }
}
