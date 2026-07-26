package com.esmpf;

import static com.esmpf.catalog.CatalogDtos.EquipmentTypeCommand;
import static com.esmpf.customer.CustomerDtos.CustomerCreateCommand;
import static com.esmpf.customer.CustomerDtos.ServiceLocationCreateCommand;
import static com.esmpf.equipment.EquipmentDtos.EquipmentCreateCommand;
import static com.esmpf.equipment.EquipmentDtos.EquipmentUpdateCommand;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.esmpf.catalog.CatalogService;
import com.esmpf.customer.CustomerService;
import com.esmpf.equipment.EquipmentDtos.EquipmentResponse;
import com.esmpf.equipment.EquipmentService;
import com.esmpf.shared.exception.EntityNotFoundException;
import com.esmpf.shared.exception.StaleEntityException;
import com.esmpf.shared.tenant.TenantContext;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Import(ServiceLayerIntegrationTests.TenantTestConfiguration.class)
@Transactional
class ServiceLayerIntegrationTests {

    private static final UUID TENANT_A = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID TENANT_B = UUID.fromString("00000000-0000-0000-0000-000000000202");
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000303");

    private final CustomerService customerService;
    private final CatalogService catalogService;
    private final EquipmentService equipmentService;
    private final MutableTenantContext tenantContext;

    @Autowired
    ServiceLayerIntegrationTests(
            CustomerService customerService,
            CatalogService catalogService,
            EquipmentService equipmentService,
            MutableTenantContext tenantContext
    ) {
        this.customerService = customerService;
        this.catalogService = catalogService;
        this.equipmentService = equipmentService;
        this.tenantContext = tenantContext;
    }

    @BeforeEach
    void selectTenant() {
        tenantContext.businessId = TENANT_A;
        tenantContext.userId = USER_ID;
    }

    @Test
    void createsConsistentEquipmentGraphAndEnforcesTenantIsolation() {
        var customer = customerService.createCustomer(new CustomerCreateCommand(
                "COMPANY", "Acme", "+77010000000", "office@acme.test", "ru",
                null, null, null, null));

        var location = customerService.createServiceLocation(new ServiceLocationCreateCommand(
                customer.id(), null, "Main workshop", "WORKSHOP", "Almaty",
                43.2389, 76.8897, "Asia/Almaty", null));

        var equipmentType = catalogService.createEquipmentType(new EquipmentTypeCommand(
                0, "PUMP", "Industrial pump", "ROTATING", 1,
                null, null, null));

        EquipmentResponse equipment = equipmentService.createEquipment(new EquipmentCreateCommand(
                customer.id(), location.id(), equipmentType.id(), null,
                "Pump 01", "Vendor", "Model X", "SER-001", "ASSET-001",
                null, null, null, null, null));

        assertEquals(customer.id(), equipment.customerId());
        assertEquals(location.id(), equipment.serviceLocationId());
        assertEquals("ACTIVE", equipment.status());

        tenantContext.businessId = TENANT_B;
        assertThrows(EntityNotFoundException.class, () -> equipmentService.getEquipment(equipment.id()));
    }

    @Test
    void rejectsStaleEquipmentUpdate() {
        var customer = customerService.createCustomer(new CustomerCreateCommand(
                "PERSON", "Customer", null, null, "ru", null, null, null, null));
        var location = customerService.createServiceLocation(new ServiceLocationCreateCommand(
                customer.id(), null, "Home", "HOME", null, null, null,
                "Asia/Almaty", null));
        var equipmentType = catalogService.createEquipmentType(new EquipmentTypeCommand(
                0, "BOILER", "Boiler", "HEATING", 1, null, null, null));
        var equipment = equipmentService.createEquipment(new EquipmentCreateCommand(
                customer.id(), location.id(), equipmentType.id(), null,
                "Boiler 01", null, null, "SER-002", null,
                null, null, null, null, null));

        var staleUpdate = new EquipmentUpdateCommand(
                equipment.version() + 100, null, null, "Changed", null, null,
                null, null, null, null, null, null, null);

        assertThrows(StaleEntityException.class,
                () -> equipmentService.updateEquipment(equipment.id(), staleUpdate));
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class TenantTestConfiguration {
        @Bean
        @Primary
        MutableTenantContext mutableTenantContext() {
            return new MutableTenantContext();
        }
    }

    static final class MutableTenantContext implements TenantContext {
        private UUID businessId;
        private UUID userId;

        @Override
        public UUID requireBusinessId() {
            return businessId;
        }

        @Override
        public UUID requireUserId() {
            return userId;
        }
    }
}
