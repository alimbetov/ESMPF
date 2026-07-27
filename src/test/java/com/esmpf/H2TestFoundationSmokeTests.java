package com.esmpf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.esmpf.identity.RoleProvisioningService;
import com.esmpf.support.BusinessTestFixture;
import java.sql.Connection;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class H2TestFoundationSmokeTests {

    @Autowired
    DataSource dataSource;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    BusinessTestFixture businessFixture;

    @Autowired
    RoleProvisioningService roleProvisioningService;

    @Test
    void fastTestContextUsesH2() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            assertEquals("H2", connection.getMetaData().getDatabaseProductName());
        }
    }

    @Test
    void provisionsAndReusesRoleOnH2() {
        UUID businessId = businessFixture.createActiveBusiness();

        var first = roleProvisioningService.ensureRole(
                businessId, "viewer", "Viewer", "Read-only test role", true);
        var second = roleProvisioningService.ensureRole(
                businessId, "VIEWER", "Ignored", null, false);

        assertEquals(first.id(), second.id());
        assertTrue(second.active());
        Integer count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM access_role WHERE business_id=? AND code='VIEWER'",
                Integer.class,
                businessId);
        assertEquals(1, count);
    }
}
