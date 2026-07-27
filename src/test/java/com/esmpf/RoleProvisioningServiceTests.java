package com.esmpf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.esmpf.identity.RoleProvisioningService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class RoleProvisioningServiceTests {

    @Autowired
    RoleProvisioningService roleProvisioningService;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void createsMissingRoleAndReusesItCaseInsensitively() {
        UUID businessId = createBusiness();

        var created = roleProvisioningService.ensureRole(
                businessId,
                "technician",
                "Technician",
                "Executes assigned service work",
                true);

        var reused = roleProvisioningService.ensureRole(
                businessId,
                "TECHNICIAN",
                "Different ignored name",
                null,
                false);

        assertEquals(created.id(), reused.id());
        assertEquals("TECHNICIAN", reused.code());
        assertEquals("Technician", reused.name());
        assertTrue(reused.system());
        assertTrue(reused.active());
        assertEquals(1, countRoles(businessId, "TECHNICIAN"));
    }

    @Test
    void sameRoleCodeIsIndependentBetweenTenants() {
        UUID firstBusiness = createBusiness();
        UUID secondBusiness = createBusiness();

        var first = roleProvisioningService.ensureRole(
                firstBusiness, "VIEWER", "Viewer", null, true);
        var second = roleProvisioningService.ensureRole(
                secondBusiness, "VIEWER", "Viewer", null, true);

        assertNotEquals(first.id(), second.id());
        assertEquals(firstBusiness, first.businessId());
        assertEquals(secondBusiness, second.businessId());
    }

    @Test
    void rejectsInvalidProvisioningInput() {
        UUID businessId = createBusiness();

        assertThrows(IllegalArgumentException.class, () -> roleProvisioningService.ensureRole(
                businessId, "bad-role", "Bad role", null, false));
        assertThrows(IllegalArgumentException.class, () -> roleProvisioningService.ensureRole(
                businessId, "ADMIN", " ", null, true));
        assertThrows(IllegalArgumentException.class, () -> roleProvisioningService.ensureRole(
                null, "ADMIN", "Admin", null, true));
    }

    private UUID createBusiness() {
        UUID id = UUID.randomUUID();
        String code = "RBAC-" + UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO business(
                    id,created_at,updated_at,version,name,code,timezone,
                    default_language,currency,status
                ) VALUES (?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,0,?,?,'Asia/Almaty','ru','KZT','ACTIVE')
                """, id, code, code);
        return id;
    }

    private int countRoles(UUID businessId, String code) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM access_role WHERE business_id=? AND upper(code)=?",
                Integer.class,
                businessId,
                code);
        return count == null ? 0 : count;
    }
}
