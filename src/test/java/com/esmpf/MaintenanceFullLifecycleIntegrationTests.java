package com.esmpf;

import static com.esmpf.catalog.CatalogDtos.ChecklistTemplateCommand;
import static com.esmpf.catalog.CatalogDtos.EquipmentTypeCommand;
import static com.esmpf.catalog.CatalogDtos.JobTypeCommand;
import static com.esmpf.catalog.CatalogDtos.MaintenanceTemplateCommand;
import static com.esmpf.customer.CustomerDtos.CustomerCreateCommand;
import static com.esmpf.customer.CustomerDtos.ServiceLocationCreateCommand;
import static com.esmpf.equipment.EquipmentDtos.EquipmentCreateCommand;
import static com.esmpf.maintenance.MaintenanceDtos.MaintenanceOccurrenceCreateCommand;
import static com.esmpf.maintenance.MaintenanceDtos.MaintenancePlanCreateCommand;
import static com.esmpf.service.ServiceManagementDtos.JobExecutionStartCommand;
import static com.esmpf.service.ServiceManagementDtos.JobVisitPlanCommand;
import static com.esmpf.service.ServiceManagementDtos.ServiceJobCreateCommand;
import static com.esmpf.service.ServiceManagementDtos.WorkReportCreateCommand;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
@Import(MaintenanceFullLifecycleIntegrationTests.TenantTestConfiguration.class)
@Transactional
class MaintenanceFullLifecycleIntegrationTests {

    private static final UUID TENANT = UUID.fromString("00000000-0000-0000-0000-000000000611");
    private static final UUID USER = UUID.fromString("00000000-0000-0000-0000-000000000622");

    private final CustomerService customerService;
    private final CatalogService catalogService;
    private final EquipmentService equipmentService;
    private final MaintenanceService maintenanceService;
    private final ServiceManagementService serviceManagementService;
    private final MutableTenantContext tenantContext;

    @Autowired
    MaintenanceFullLifecycleIntegrationTests(
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
    void completesMaintenanceOccurrenceThroughClosedServiceJob() {
        Fixture fixture = createFixture();

        var plan = maintenanceService.createPlan(new MaintenancePlanCreateCommand(
                fixture.equipmentId(), fixture.maintenanceTemplateId(), LocalDate.now(), null,
                LocalDate.now().plusDays(30), null, null));
        assertEquals("DRAFT", plan.status());

        plan = maintenanceService.activatePlan(plan.id(), plan.version());
        plan = maintenanceService.getPlan(plan.id());
        assertEquals("ACTIVE", plan.status());

        var occurrenceCommand = new MaintenanceOccurrenceCreateCommand(
                plan.id(), LocalDate.now().plusDays(30), null,
                "maintenance:" + UUID.randomUUID(), "Scheduled preventive maintenance");
        var occurrence = maintenanceService.generateOccurrence(occurrenceCommand);
        assertEquals("PLANNED", occurrence.status());
        assertThrows(IllegalArgumentException.class,
                () -> maintenanceService.generateOccurrence(occurrenceCommand));

        var job = serviceManagementService.createJob(new ServiceJobCreateCommand(
                null, occurrence.id(), fixture.customerId(), fixture.locationId(),
                fixture.equipmentId(), fixture.jobTypeId(), null,
                "NORMAL", "Preventive maintenance", "Scheduled maintenance visit", null, null));

        occurrence = maintenanceService.linkServiceJob(occurrence.id(), occurrence.version(), job.id());
        occurrence = maintenanceService.getOccurrence(occurrence.id());
        assertEquals("JOB_CREATED", occurrence.status());
        assertEquals(job.id(), occurrence.serviceJobId());

        UUID linkedOccurrenceId = occurrence.id();
        long linkedVersion = occurrence.version();
        assertThrows(IllegalStateException.class,
                () -> maintenanceService.completeOccurrence(linkedOccurrenceId, linkedVersion));

        job = serviceManagementService.markJobReady(job.id(), job.version());
        job = serviceManagementService.getJob(job.id());

        var visit = serviceManagementService.planVisit(new JobVisitPlanCommand(
                job.id(), Instant.now().plusSeconds(3600), Instant.now().plusSeconds(7200), null));
        visit = serviceManagementService.startVisit(visit.id(), visit.version(), "{\"arrived\":true}");

        var execution = serviceManagementService.startExecution(new JobExecutionStartCommand(
                job.id(), visit.id(), fixture.checklistId(), fixture.checklistVersion(), fixture.checklistSchema()));
        execution = serviceManagementService.completeExecution(
                execution.id(), execution.version(), "{\"result\":\"PASS\"}");

        visit = serviceManagementService.completeVisit(
                visit.id(), visit.version(), "{\"completed\":true}", "{\"accepted\":true}");

        var report = serviceManagementService.createWorkReport(new WorkReportCreateCommand(
                job.id(), visit.id(), execution.id(), "Preventive inspection",
                "Inspection and adjustment completed", "Equipment operational", "[]", "[]", "Accepted"));
        report = serviceManagementService.approveWorkReport(report.id(), report.version());
        assertEquals("APPROVED", report.status());

        job = serviceManagementService.getJob(job.id());
        job = serviceManagementService.completeJob(job.id(), job.version());
        job = serviceManagementService.getJob(job.id());
        job = serviceManagementService.closeJob(job.id(), job.version());
        assertEquals("CLOSED", job.status());

        occurrence = maintenanceService.getOccurrence(linkedOccurrenceId);
        occurrence = maintenanceService.completeOccurrence(occurrence.id(), occurrence.version());
        occurrence = maintenanceService.getOccurrence(occurrence.id());
        assertEquals("COMPLETED", occurrence.status());
        assertTrue(occurrence.completedAt() != null);
        assertTrue(maintenanceService.getPlan(plan.id()).lastCompletedAt() != null);
    }

    @Test
    void rejectsServiceJobThatDoesNotReferenceOccurrence() {
        Fixture fixture = createFixture();
        var plan = maintenanceService.createPlan(new MaintenancePlanCreateCommand(
                fixture.equipmentId(), fixture.maintenanceTemplateId(), LocalDate.now(), null,
                LocalDate.now().plusDays(30), null, null));
        plan = maintenanceService.activatePlan(plan.id(), plan.version());
        plan = maintenanceService.getPlan(plan.id());

        var occurrence = maintenanceService.generateOccurrence(new MaintenanceOccurrenceCreateCommand(
                plan.id(), LocalDate.now().plusDays(30), null,
                "maintenance:" + UUID.randomUUID(), "Scheduled preventive maintenance"));

        var unrelatedJob = serviceManagementService.createJob(new ServiceJobCreateCommand(
                null, null, fixture.customerId(), fixture.locationId(), fixture.equipmentId(),
                fixture.jobTypeId(), null, "NORMAL", "Unrelated job", null, null, null));

        long occurrenceVersion = occurrence.version();
        UUID occurrenceId = occurrence.id();
        assertThrows(IllegalArgumentException.class,
                () -> maintenanceService.linkServiceJob(occurrenceId, occurrenceVersion, unrelatedJob.id()));
    }

    private Fixture createFixture() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        var customer = customerService.createCustomer(new CustomerCreateCommand(
                "COMPANY", "Maintenance customer " + suffix, null, null, "ru",
                null, null, null, null));
        var location = customerService.createServiceLocation(new ServiceLocationCreateCommand(
                customer.id(), null, "Maintenance site " + suffix, "SITE", null,
                null, null, "Asia/Almaty", null));
        var equipmentType = catalogService.createEquipmentType(new EquipmentTypeCommand(
                0, "MTYPE-" + suffix, "Maintenance equipment " + suffix,
                "GENERAL", 1, null, null, null));
        var jobType = catalogService.createJobType(new JobTypeCommand(
                0, "MJOB-" + suffix, "Preventive job " + suffix,
                "MAINTENANCE", 60, new BigDecimal("10000"), true, false, true, null));
        var checklist = catalogService.createChecklistTemplate(new ChecklistTemplateCommand(
                0, "MCHECK-" + suffix, "Maintenance checklist " + suffix,
                equipmentType.id(), jobType.id(), 1, "{\"required\":[\"result\"]}"));
        checklist = catalogService.publishChecklistTemplate(checklist.id(), checklist.version());
        var maintenanceTemplate = catalogService.createMaintenanceTemplate(new MaintenanceTemplateCommand(
                0, "PM-" + suffix, "Preventive maintenance " + suffix,
                equipmentType.id(), jobType.id(), checklist.id(), 1,
                "{\"periodDays\":30}", null, null));
        var equipment = equipmentService.createEquipment(new EquipmentCreateCommand(
                customer.id(), location.id(), equipmentType.id(), null,
                "Equipment " + suffix, null, null, "SER-" + suffix,
                "ASSET-" + suffix, null, null, null, null, null));
        return new Fixture(customer.id(), location.id(), equipment.id(), jobType.id(),
                maintenanceTemplate.id(), checklist.id(), checklist.templateVersion(), checklist.schemaJson());
    }

    private record Fixture(
            UUID customerId,
            UUID locationId,
            UUID equipmentId,
            UUID jobTypeId,
            UUID maintenanceTemplateId,
            UUID checklistId,
            Integer checklistVersion,
            String checklistSchema
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