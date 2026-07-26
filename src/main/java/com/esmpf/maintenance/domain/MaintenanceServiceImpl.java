package com.esmpf.maintenance.domain;

import static com.esmpf.maintenance.MaintenanceDtos.*;

import com.esmpf.catalog.CatalogDtos.MaintenanceTemplateReference;
import com.esmpf.catalog.CatalogReferenceQuery;
import com.esmpf.equipment.EquipmentDtos.EquipmentReference;
import com.esmpf.equipment.EquipmentReferenceQuery;
import com.esmpf.maintenance.MaintenanceReferenceQuery;
import com.esmpf.maintenance.MaintenanceService;
import com.esmpf.shared.exception.EntityNotFoundException;
import com.esmpf.shared.exception.StaleEntityException;
import com.esmpf.shared.tenant.TenantContext;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class MaintenanceServiceImpl implements MaintenanceService, MaintenanceReferenceQuery {

    private final TenantContext tenantContext;
    private final EquipmentReferenceQuery equipmentReferences;
    private final CatalogReferenceQuery catalogReferences;
    private final MaintenancePlanRepository planRepository;
    private final MaintenanceOccurrenceRepository occurrenceRepository;
    private final MaintenanceMapper mapper;

    @Override
    @Transactional
    public MaintenancePlanResponse createPlan(MaintenancePlanCreateCommand command) {
        EquipmentReference equipment = equipmentReferences.requireEquipment(command.equipmentId());
        requireActive(equipment.status(), "Equipment");
        MaintenanceTemplateReference template = catalogReferences.requireMaintenanceTemplate(command.maintenanceTemplateId());
        requireActive(template.status(), "MaintenanceTemplate");
        if (!template.equipmentTypeId().equals(equipment.equipmentTypeId())) {
            throw new IllegalArgumentException("Maintenance template does not match equipment type");
        }
        validatePlanDates(command.activeFrom(), command.activeUntil(), command.nextDueDate());
        if (planRepository.existsByBusinessIdAndEquipmentIdAndMaintenanceTemplateIdAndStatusIn(
                tenant(), command.equipmentId(), command.maintenanceTemplateId(),
                List.of("DRAFT", "ACTIVE", "SUSPENDED"))) {
            throw new IllegalArgumentException("An open maintenance plan already exists for this equipment and template");
        }
        MaintenancePlan entity = mapper.toEntity(command);
        entity.setBusinessId(tenant());
        entity.setTemplateVersion(template.templateVersion());
        if (entity.getActiveFrom() == null) {
            entity.setActiveFrom(LocalDate.now());
        }
        return mapper.toResponse(planRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public MaintenancePlanResponse getPlan(UUID id) {
        return mapper.toResponse(requirePlanEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MaintenancePlanResponse> listPlans(Pageable pageable) {
        return planRepository.findAllByBusinessId(tenant(), pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public MaintenancePlanResponse updatePlan(UUID id, MaintenancePlanUpdateCommand command) {
        MaintenancePlan entity = requirePlanEntity(id);
        checkVersion("MaintenancePlan", id, command.version(), entity.getVersion());
        if ("CLOSED".equals(entity.getStatus())) {
            throw new IllegalStateException("Closed maintenance plan is immutable");
        }
        LocalDate activeFrom = command.activeFrom() == null ? entity.getActiveFrom() : command.activeFrom();
        LocalDate activeUntil = command.activeUntil() == null ? entity.getActiveUntil() : command.activeUntil();
        LocalDate nextDueDate = command.nextDueDate() == null ? entity.getNextDueDate() : command.nextDueDate();
        validatePlanDates(activeFrom, activeUntil, nextDueDate);
        mapper.update(command, entity);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public MaintenancePlanResponse activatePlan(UUID id, long version) {
        MaintenancePlan entity = requirePlanEntity(id);
        checkVersion("MaintenancePlan", id, version, entity.getVersion());
        if (!("DRAFT".equals(entity.getStatus()) || "SUSPENDED".equals(entity.getStatus()))) {
            throw new IllegalStateException("Only DRAFT or SUSPENDED maintenance plans can be activated");
        }
        entity.setStatus("ACTIVE");
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public MaintenancePlanResponse suspendPlan(UUID id, long version) {
        MaintenancePlan entity = requirePlanEntity(id);
        checkVersion("MaintenancePlan", id, version, entity.getVersion());
        if (!"ACTIVE".equals(entity.getStatus())) {
            throw new IllegalStateException("Only ACTIVE maintenance plans can be suspended");
        }
        entity.setStatus("SUSPENDED");
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public MaintenancePlanResponse closePlan(UUID id, long version) {
        MaintenancePlan entity = requirePlanEntity(id);
        checkVersion("MaintenancePlan", id, version, entity.getVersion());
        if ("CLOSED".equals(entity.getStatus())) {
            throw new IllegalStateException("Maintenance plan is already closed");
        }
        entity.setStatus("CLOSED");
        entity.setActiveUntil(LocalDate.now());
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public MaintenanceOccurrenceResponse generateOccurrence(MaintenanceOccurrenceCreateCommand command) {
        MaintenancePlan plan = requirePlanEntity(command.maintenancePlanId());
        if (!"ACTIVE".equals(plan.getStatus())) {
            throw new IllegalStateException("Occurrences can only be generated for ACTIVE plans");
        }
        if (command.dueDate() == null && command.dueMeterValue() == null) {
            throw new IllegalArgumentException("Occurrence requires dueDate or dueMeterValue");
        }
        if (command.dueMeterValue() != null && command.dueMeterValue().signum() < 0) {
            throw new IllegalArgumentException("Due meter value cannot be negative");
        }
        if (occurrenceRepository.existsByBusinessIdAndMaintenancePlanIdAndGenerationKey(
                tenant(), command.maintenancePlanId(), command.generationKey())) {
            throw new IllegalArgumentException("Maintenance occurrence generation key already exists");
        }
        MaintenanceOccurrence entity = mapper.toEntity(command);
        entity.setBusinessId(tenant());
        entity.setGeneratedAt(Instant.now());
        return mapper.toResponse(occurrenceRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public MaintenanceOccurrenceResponse getOccurrence(UUID id) {
        return mapper.toResponse(requireOccurrenceEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MaintenanceOccurrenceResponse> listOccurrences(UUID planId, Pageable pageable) {
        requirePlanEntity(planId);
        return occurrenceRepository
                .findAllByBusinessIdAndMaintenancePlanId(tenant(), planId, pageable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional
    public MaintenanceOccurrenceResponse linkServiceJob(UUID occurrenceId, long version, UUID serviceJobId) {
        MaintenanceOccurrence entity = requireOccurrenceEntity(occurrenceId);
        checkVersion("MaintenanceOccurrence", occurrenceId, version, entity.getVersion());
        if (entity.getServiceJobId() != null) {
            throw new IllegalStateException("Maintenance occurrence is already linked to a service job");
        }
        if (!("PLANNED".equals(entity.getStatus()) || "DUE".equals(entity.getStatus()))) {
            throw new IllegalStateException("Maintenance occurrence cannot be linked in status " + entity.getStatus());
        }
        entity.setServiceJobId(serviceJobId);
        entity.setStatus("JOB_CREATED");
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public MaintenanceOccurrenceResponse completeOccurrence(UUID occurrenceId, long version) {
        MaintenanceOccurrence entity = requireOccurrenceEntity(occurrenceId);
        checkVersion("MaintenanceOccurrence", occurrenceId, version, entity.getVersion());
        if (!"JOB_CREATED".equals(entity.getStatus())) {
            throw new IllegalStateException("Only JOB_CREATED occurrences can be completed");
        }
        Instant now = Instant.now();
        entity.setStatus("COMPLETED");
        entity.setCompletedAt(now);
        MaintenancePlan plan = requirePlanEntity(entity.getMaintenancePlanId());
        plan.setLastCompletedAt(now);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public MaintenanceOccurrenceResponse cancelOccurrence(UUID occurrenceId, long version, String reason) {
        MaintenanceOccurrence entity = requireOccurrenceEntity(occurrenceId);
        checkVersion("MaintenanceOccurrence", occurrenceId, version, entity.getVersion());
        if ("COMPLETED".equals(entity.getStatus()) || "CANCELLED".equals(entity.getStatus())) {
            throw new IllegalStateException("Terminal maintenance occurrence cannot be cancelled");
        }
        entity.setStatus("CANCELLED");
        entity.setReason(reason);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public MaintenancePlanReference requirePlan(UUID planId) {
        MaintenancePlan entity = requirePlanEntity(planId);
        return new MaintenancePlanReference(
                entity.getId(), entity.getEquipmentId(), entity.getMaintenanceTemplateId(), entity.getStatus());
    }

    @Override
    @Transactional(readOnly = true)
    public MaintenanceOccurrenceReference requireOccurrence(UUID occurrenceId) {
        MaintenanceOccurrence entity = requireOccurrenceEntity(occurrenceId);
        return new MaintenanceOccurrenceReference(
                entity.getId(), entity.getMaintenancePlanId(), entity.getServiceJobId(), entity.getStatus());
    }

    private UUID tenant() {
        return tenantContext.requireBusinessId();
    }

    private MaintenancePlan requirePlanEntity(UUID id) {
        return planRepository.findByIdAndBusinessId(id, tenant())
                .orElseThrow(() -> new EntityNotFoundException("MaintenancePlan", id));
    }

    private MaintenanceOccurrence requireOccurrenceEntity(UUID id) {
        return occurrenceRepository.findByIdAndBusinessId(id, tenant())
                .orElseThrow(() -> new EntityNotFoundException("MaintenanceOccurrence", id));
    }

    private static void validatePlanDates(LocalDate activeFrom, LocalDate activeUntil, LocalDate nextDueDate) {
        if (activeFrom != null && activeUntil != null && activeUntil.isBefore(activeFrom)) {
            throw new IllegalArgumentException("activeUntil cannot be before activeFrom");
        }
        if (activeUntil != null && nextDueDate != null && nextDueDate.isAfter(activeUntil)) {
            throw new IllegalArgumentException("nextDueDate cannot be after activeUntil");
        }
    }

    private static void requireActive(String status, String entity) {
        if (!"ACTIVE".equals(status)) {
            throw new IllegalStateException(entity + " is not active");
        }
    }

    private static void checkVersion(String name, UUID id, long expected, long actual) {
        if (expected != actual) {
            throw new StaleEntityException(name, id, expected, actual);
        }
    }
}
