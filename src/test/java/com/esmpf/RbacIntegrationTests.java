package com.esmpf;

import static com.esmpf.identity.IdentityDtos.BusinessCreateCommand;
import static com.esmpf.identity.IdentityDtos.UserAccountCreateCommand;
import static com.esmpf.identity.RbacDtos.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.esmpf.identity.AccessControlQuery;
import com.esmpf.identity.IdentityService;
import com.esmpf.identity.PermissionCode;
import com.esmpf.identity.RbacService;
import com.esmpf.shared.tenant.TenantContext;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Import(RbacIntegrationTests.TestConfig.class)
@Transactional
class RbacIntegrationTests {

    @Autowired IdentityService identityService;
    @Autowired RbacService rbacService;
    @Autowired AccessControlQuery accessControlQuery;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired MutableTenantContext tenantContext;

    @Test
    void resolvesOnlyActiveTenantRolePermissionsAndRevokesAssignment() {
        var business = identityService.createBusiness(new BusinessCreateCommand(
                "RBAC Test", "rbac-" + UUID.randomUUID(), "Asia/Almaty", "ru", "KZT", "{}"));
        tenantContext.businessId = business.id();

        var user = identityService.createUser(new UserAccountCreateCommand(
                "rbac-" + UUID.randomUUID() + "@example.test", null, "RBAC User", true));
        tenantContext.userId = user.id();

        seed(PermissionCode.CUSTOMER_READ);
        seed(PermissionCode.SERVICE_JOB_EXECUTE);

        var role = rbacService.createRole(new RoleCommand(0, "FIELD_ENGINEER", "Field engineer", null));
        role = rbacService.replacePermissions(role.id(), new ReplaceRolePermissionsCommand(
                role.version(), Set.of(PermissionCode.CUSTOMER_READ, PermissionCode.SERVICE_JOB_EXECUTE)));
        var assignment = rbacService.assignRole(user.id(), new RoleAssignmentCommand(role.id(), null, null));

        var access = accessControlQuery.resolveEffectiveAccess(user.id());
        assertEquals(business.id(), access.businessId());
        assertTrue(access.roleCodes().contains("FIELD_ENGINEER"));
        assertTrue(access.permissions().contains(PermissionCode.CUSTOMER_READ));
        assertTrue(accessControlQuery.hasPermission(user.id(), PermissionCode.SERVICE_JOB_EXECUTE));

        rbacService.revokeAssignment(assignment.id(), assignment.version());
        var revoked = accessControlQuery.resolveEffectiveAccess(user.id());
        assertFalse(revoked.roleCodes().contains("FIELD_ENGINEER"));
        assertFalse(revoked.permissions().contains(PermissionCode.CUSTOMER_READ));
    }

    @Test
    void rejectsDuplicateActiveAssignmentAndInvalidValidityWindow() {
        var business = identityService.createBusiness(new BusinessCreateCommand(
                "RBAC Duplicate", "rbac-dup-" + UUID.randomUUID(), "Asia/Almaty", "ru", "KZT", "{}"));
        tenantContext.businessId = business.id();
        var user = identityService.createUser(new UserAccountCreateCommand(
                "dup-" + UUID.randomUUID() + "@example.test", null, "Duplicate User", false));
        tenantContext.userId = user.id();
        var role = rbacService.createRole(new RoleCommand(0, "CUSTOM_ROLE", "Custom role", null));

        rbacService.assignRole(user.id(), new RoleAssignmentCommand(role.id(), null, null));
        assertThrows(IllegalArgumentException.class,
                () -> rbacService.assignRole(user.id(), new RoleAssignmentCommand(role.id(), null, null)));
        assertThrows(IllegalArgumentException.class,
                () -> rbacService.assignRole(user.id(), new RoleAssignmentCommand(
                        role.id(), Instant.parse("2026-08-02T00:00:00Z"), Instant.parse("2026-08-01T00:00:00Z"))));
    }

    private void seed(PermissionCode code) {
        jdbcTemplate.update("INSERT INTO permission(code,category,description) VALUES (?,?,?)",
                code.name(), code.name().split("_")[0], code.name());
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class TestConfig {
        @Bean @Primary MutableTenantContext mutableTenantContext() { return new MutableTenantContext(); }
    }

    static final class MutableTenantContext implements TenantContext {
        private UUID businessId;
        private UUID userId;
        @Override public UUID requireBusinessId() { return businessId; }
        @Override public UUID requireUserId() { return userId; }
    }
}
