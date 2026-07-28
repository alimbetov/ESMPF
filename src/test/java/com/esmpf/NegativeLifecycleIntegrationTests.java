package com.esmpf;

import static com.esmpf.identity.IdentityDtos.BusinessCreateCommand;
import static com.esmpf.identity.IdentityDtos.BusinessLocationCommand;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.esmpf.identity.IdentityService;
import com.esmpf.shared.tenant.TenantContext;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Import(NegativeLifecycleIntegrationTests.TenantTestConfiguration.class)
@Transactional
class NegativeLifecycleIntegrationTests {

    @Autowired IdentityService identityService;
    @Autowired MutableTenantContext tenantContext;

    @Test
    void rejectsRepeatedBusinessLocationLifecycleTransitions() {
        var business = identityService.createBusiness(new BusinessCreateCommand(
                "Lifecycle Test", "life-" + UUID.randomUUID(), "Asia/Almaty", "ru", "KZT", "{}"));
        tenantContext.businessId = business.id();
        tenantContext.userId = UUID.randomUUID();

        var location = identityService.createLocation(new BusinessLocationCommand(
                0, "Office", "Almaty", 43.238949, 76.889709, "Asia/Almaty"));
        var inactive = identityService.deactivateLocation(location.id(), location.version());

        assertThrows(IllegalStateException.class,
                () -> identityService.deactivateLocation(inactive.id(), inactive.version()));

        var active = identityService.activateLocation(inactive.id(), inactive.version());
        assertThrows(IllegalStateException.class,
                () -> identityService.activateLocation(active.id(), active.version()));
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class TenantTestConfiguration {
        @Bean @Primary MutableTenantContext mutableTenantContext() { return new MutableTenantContext(); }
    }

    static final class MutableTenantContext implements TenantContext {
        private UUID businessId;
        private UUID userId;
        @Override public UUID requireBusinessId() { return businessId; }
        @Override public UUID requireUserId() { return userId; }
    }
}
