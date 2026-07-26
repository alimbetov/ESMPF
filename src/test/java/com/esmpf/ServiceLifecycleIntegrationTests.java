package com.esmpf;

import static com.esmpf.catalog.CatalogDtos.EquipmentTypeCommand;
import static com.esmpf.catalog.CatalogDtos.JobTypeCommand;
import static com.esmpf.catalog.CatalogDtos.MaintenanceTemplateCommand;
import static com.esmpf.customer.CustomerDtos.CustomerCreateCommand;
import static com.esmpf.customer.CustomerDtos.ServiceLocationCreateCommand;
import static com.esmpf.equipment.EquipmentDtos.EquipmentCreateCommand;
import static com.esmpf.maintenance.MaintenanceDtos.MaintenanceOccurrenceCreateCommand;
import static com.esmpf.maintenance.MaintenanceDtos.MaintenancePlanCreateCommand;
import static com.esmpf.service.ServiceManagementDtos.JobVisitPlanCommand;
import static com.esmpf.service.ServiceManagementDtos.ServiceJobCreateCommand;
import static com.esmpf.service.ServiceManagementDtos.ServiceRequestCreateCommand;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.esmpf.catalog.CatalogService;
import com.esmpf.customer.CustomerService;
import com.esmpf.equipment.EquipmentService;
import com.esmpf.maintenance.MaintenanceService;
import com.esmpf.service.ServiceManagementService;
import com.esmpf.shared.tenant.TenantContext;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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
@Import(ServiceLifecycleIntegrationTests.TenantTestConfiguration.class)
@Transactional
class ServiceLifecycleIntegrationTests {

    private static final UUID TENANT = UUID.fromString("00000000-0000-0000-0000-000000000411");
    private static final UUID USER = UUID.fromString("00000000-0000-0000-0000-000000000422");

    private final CustomerService customerService;
    private final CatalogService catalogService;
    private final EquipmentService equipmentService;
    private final MaintenanceService maintenanceService;
    private final ServiceManagementService serviceManagementService;
    private final MutableTenantContext tenantContext;

    @Autowired
    ServiceLifecycleIntegrationTests(
            CustomerService customerService,
            CatalogService catalogService,
            EquipmentService equipmentService,
            MaintenanceService maintenanceService,
            ServiceManagementService serviceManagementService,
            MutableTenantContext tenantContext
    ) {
        this.customerService = customerService;
        this.catalogService = catalogService;
        this.equipmentService = equipmentService;
        this.maintenanceService = maintenanceService;
        this.serviceManagementService = serviceManagementService;
        this.tenantContext = tenantContext;
    }

    @BeforeEach
    void configureTenant() {
        tenantContext.businessId = TENANT;
        tenantContext.userId = USER;
    }

    @Test
    void movesRequestIntoJobAndStartsVisit() {
        Fixture fixture = createFixture("FLOW");

        var request = serviceManagementService.createRequest(new ServiceRequestCreateCommand(
                fixture.customerId, fixture.locationId, fixture.equipmentId,
                "PHONE", "NORMAL", "Pump noise", "Customer reports abnormal noise"));
        assertEquals("NEW", request.status());

        serviceManagementService.triageRequest(request.id(), request.version());
        request = serviceManagementService.getRequest(request.id());
        serviceManagementService.acceptRequest(request.id(), request.version());
        request = serviceManagementService.getRequest(request.id());

        var job = serviceManagementService.convertRequestToJob(
                request.id(), request.version(),
                new ServiceJobCreateCommand(
                        request.id(), null, fixture.customerId, fixture.locationId,
                        fixture.equipmentId, fixture.jobTypeId, null,
                        "NORMAL", "Inspect pump", "Diagnostic visit", null, null));
        assertEquals("DRAFT", job.status());

        serviceManagementService.markJobReady(job.id(), job.version());
        job = serviceManagementService.getJob(job.id());

        var visit = serviceManagementService.planVisit(new JobVisitPlanCommand(
                job.id(), Instant.now().plusSeconds(3600), Instant.now().plusSeconds(7200), null));
        assertEquals("PLANNED", visit.status());

        visit = serviceManagementService.startVisit(visit.id(), visit.version(), "{\"arrived\":true}");
        assertEquals("IN_PROGRESS", visit.status());
        assertEquals("IN_PROGRESS", serviceManagementService.getJob(job.id()).status());
    }

    @Test
    void rejectsDuplicateMaintenanceOccurrenceGenerationKey() {
        Fixture fixture = createFixture("MAINT");
        var template = catalogService.createMaintenanceTemplate(new MaintenanceTemplateCommand(
                0, "PM-" + fixture.suffix, "Preventive maintenance",
                fixture.equipmentTypeId, fixture.jobTypeId, null, 1,
                "{\"periodDays\":30}", null, null));

        var plan = maintenanceService.createPlan(new MaintenancePlanCreateCommand(
                fixture.equipmentId, template.id(), LocalDate.now(), null,
                LocalDate.now().plusDays(30), new BigDecimal("1000"), null));
        plan = maintenanceService.activatePlan(plan.id(), plan.version());

        var command = new MaintenanceOccurrenceCreateCommand(
                plan.id(), LocalDate.now().plusDays(30), null,
                plan.id() + ":2026-08", "monthly generation");
        maintenanceService.generateOccurrence(command);

        assertThrows(IllegalArgumentException.class,
                () -> maintenanceService.generateOccurrence(command));
    }

    private Fixture createFixture(String suffix) {
        var customer = customerService.createCustomer(new CustomerCreateCommand(
                "COMPANY", "Customer " + suffix, null, null, "ru",
                null, null, null, null));
        var location = customerService.createServiceLocation(new ServiceLocationCreateCommand(
                customer.id(), null, "Location " + suffix, "SITE", null,
                null, null, "Asia/Almaty", null));
        var equipmentType = catalogService.createEquipmentType(new EquipmentTypeCommand(
                0, "TYPE-" + suffix, "Equipment type " + suffix,
                "GENERAL", 1, null, null, null));
        var jobType = catalogService.createJobType(new JobTypeCommand(
                0, "JOB-" + suffix, "Job type " + suffix,
                "SERVICE", 60, new BigDecimal("10000"),
                false, false, false, null));
        var equipment = equipmentService.createEquipment(new EquipmentCreateCommand(
                customer.id(), location.id(), equipmentType.id(), null,
                "Equipment " + suffix, null, null, "SER-" + suffix,
                "ASSET-" + suffix, null, null, null, null, null));
        return new Fixture(
                suffix, customer.id(), location.id(), equipment.id(),
                equipmentType.id(), jobType.id());
    }

    private record Fixture(
            String suffix,
            UUID customerId,
            UUID locationId,
            UUID equipmentId,
            UUID equipmentTypeId,
            UUID jobTypeId
    ) {
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
