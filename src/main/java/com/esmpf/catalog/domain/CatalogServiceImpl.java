package com.esmpf.catalog.domain;

import static com.esmpf.catalog.CatalogDtos.*;

import com.esmpf.catalog.CatalogReferenceQuery;
import com.esmpf.catalog.CatalogService;
import com.esmpf.shared.exception.EntityNotFoundException;
import com.esmpf.shared.exception.StaleEntityException;
import com.esmpf.shared.tenant.TenantContext;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class CatalogServiceImpl implements CatalogService, CatalogReferenceQuery {

    private final TenantContext tenantContext;
    private final EquipmentTypeRepository equipmentTypeRepository;
    private final JobTypeRepository jobTypeRepository;
    private final ChecklistTemplateRepository checklistTemplateRepository;
    private final MaintenanceTemplateRepository maintenanceTemplateRepository;
    private final UnitOfMeasureRepository unitRepository;
    private final CatalogMapper mapper;

    @Override
    @Transactional
    public EquipmentTypeResponse createEquipmentType(EquipmentTypeCommand command) {
        UUID tenant = tenant();
        rejectDuplicate(equipmentTypeRepository.existsByBusinessIdAndCodeIgnoreCase(tenant, command.code()), "Equipment type code");
        EquipmentType entity = mapper.toEntity(command);
        entity.setBusinessId(tenant);
        return mapper.toResponse(equipmentTypeRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public EquipmentTypeResponse getEquipmentType(UUID id) {
        return mapper.toResponse(requireEquipmentTypeEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EquipmentTypeResponse> listEquipmentTypes(Pageable pageable) {
        return equipmentTypeRepository.findAllByBusinessId(tenant(), pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public EquipmentTypeResponse updateEquipmentType(UUID id, EquipmentTypeCommand command) {
        EquipmentType entity = requireEquipmentTypeEntity(id);
        checkVersion("EquipmentType", entity.getId(), command.version(), entity.getVersion());
        requireNotArchived(entity.getStatus(), "EquipmentType");
        if (!entity.getCode().equalsIgnoreCase(command.code())) {
            rejectDuplicate(equipmentTypeRepository.existsByBusinessIdAndCodeIgnoreCase(tenant(), command.code()), "Equipment type code");
        }
        mapper.update(command, entity);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public EquipmentTypeResponse archiveEquipmentType(UUID id, long version) {
        EquipmentType entity = requireEquipmentTypeEntity(id);
        checkVersion("EquipmentType", id, version, entity.getVersion());
        entity.setStatus("ARCHIVED");
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public JobTypeResponse createJobType(JobTypeCommand command) {
        UUID tenant = tenant();
        rejectDuplicate(jobTypeRepository.existsByBusinessIdAndCodeIgnoreCase(tenant, command.code()), "Job type code");
        JobType entity = mapper.toEntity(command);
        entity.setBusinessId(tenant);
        return mapper.toResponse(jobTypeRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public JobTypeResponse getJobType(UUID id) {
        return mapper.toResponse(requireJobTypeEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobTypeResponse> listJobTypes(Pageable pageable) {
        return jobTypeRepository.findAllByBusinessId(tenant(), pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public JobTypeResponse updateJobType(UUID id, JobTypeCommand command) {
        JobType entity = requireJobTypeEntity(id);
        checkVersion("JobType", id, command.version(), entity.getVersion());
        requireNotArchived(entity.getStatus(), "JobType");
        if (!entity.getCode().equalsIgnoreCase(command.code())) {
            rejectDuplicate(jobTypeRepository.existsByBusinessIdAndCodeIgnoreCase(tenant(), command.code()), "Job type code");
        }
        mapper.update(command, entity);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public JobTypeResponse archiveJobType(UUID id, long version) {
        JobType entity = requireJobTypeEntity(id);
        checkVersion("JobType", id, version, entity.getVersion());
        entity.setStatus("ARCHIVED");
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public ChecklistTemplateResponse createChecklistTemplate(ChecklistTemplateCommand command) {
        UUID tenant = tenant();
        if (command.equipmentTypeId() != null) {
            requireActive(requireEquipmentTypeEntity(command.equipmentTypeId()).getStatus(), "EquipmentType");
        }
        if (command.jobTypeId() != null) {
            requireActive(requireJobTypeEntity(command.jobTypeId()).getStatus(), "JobType");
        }
        rejectDuplicate(checklistTemplateRepository.existsByBusinessIdAndCodeIgnoreCaseAndTemplateVersion(
                tenant, command.code(), command.templateVersion()), "Checklist template version");
        ChecklistTemplate entity = mapper.toEntity(command);
        entity.setBusinessId(tenant);
        return mapper.toResponse(checklistTemplateRepository.save(entity));
    }

    @Override
    @Transactional
    public ChecklistTemplateResponse publishChecklistTemplate(UUID id, long version) {
        ChecklistTemplate entity = requireChecklistTemplateEntity(id);
        checkVersion("ChecklistTemplate", id, version, entity.getVersion());
        if (!"DRAFT".equals(entity.getStatus())) {
            throw new IllegalStateException("Only DRAFT checklist templates can be published");
        }
        entity.setStatus("PUBLISHED");
        entity.setPublishedAt(Instant.now());
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public ChecklistTemplateResponse getChecklistTemplate(UUID id) {
        return mapper.toResponse(requireChecklistTemplateEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChecklistTemplateResponse> listChecklistTemplates(Pageable pageable) {
        return checklistTemplateRepository.findAllByBusinessId(tenant(), pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public MaintenanceTemplateResponse createMaintenanceTemplate(MaintenanceTemplateCommand command) {
        UUID tenant = tenant();
        requireActive(requireEquipmentTypeEntity(command.equipmentTypeId()).getStatus(), "EquipmentType");
        requireActive(requireJobTypeEntity(command.jobTypeId()).getStatus(), "JobType");
        if (command.checklistTemplateId() != null) {
            ChecklistTemplate checklist = requireChecklistTemplateEntity(command.checklistTemplateId());
            if (!"PUBLISHED".equals(checklist.getStatus())) {
                throw new IllegalStateException("Maintenance template requires a published checklist template");
            }
            if (checklist.getEquipmentTypeId() != null && !checklist.getEquipmentTypeId().equals(command.equipmentTypeId())) {
                throw new IllegalArgumentException("Checklist template equipment type mismatch");
            }
            if (checklist.getJobTypeId() != null && !checklist.getJobTypeId().equals(command.jobTypeId())) {
                throw new IllegalArgumentException("Checklist template job type mismatch");
            }
        }
        rejectDuplicate(maintenanceTemplateRepository.existsByBusinessIdAndCodeIgnoreCaseAndTemplateVersion(
                tenant, command.code(), command.templateVersion()), "Maintenance template version");
        MaintenanceTemplate entity = mapper.toEntity(command);
        entity.setBusinessId(tenant);
        return mapper.toResponse(maintenanceTemplateRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public MaintenanceTemplateResponse getMaintenanceTemplate(UUID id) {
        return mapper.toResponse(requireMaintenanceTemplateEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MaintenanceTemplateResponse> listMaintenanceTemplates(Pageable pageable) {
        return maintenanceTemplateRepository.findAllByBusinessId(tenant(), pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public MaintenanceTemplateResponse archiveMaintenanceTemplate(UUID id, long version) {
        MaintenanceTemplate entity = requireMaintenanceTemplateEntity(id);
        checkVersion("MaintenanceTemplate", id, version, entity.getVersion());
        entity.setStatus("ARCHIVED");
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public UnitOfMeasureResponse createUnitOfMeasure(UnitOfMeasureCommand command) {
        UUID tenant = tenant();
        rejectDuplicate(unitRepository.existsByBusinessIdAndCodeIgnoreCase(tenant, command.code()), "Unit code");
        UnitOfMeasure entity = mapper.toEntity(command);
        entity.setBusinessId(tenant);
        return mapper.toResponse(unitRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public UnitOfMeasureResponse getUnitOfMeasure(UUID id) {
        return mapper.toResponse(requireUnitEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UnitOfMeasureResponse> listUnitsOfMeasure(Pageable pageable) {
        return unitRepository.findAllByBusinessId(tenant(), pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public UnitOfMeasureResponse updateUnitOfMeasure(UUID id, UnitOfMeasureCommand command) {
        UnitOfMeasure entity = requireUnitEntity(id);
        checkVersion("UnitOfMeasure", id, command.version(), entity.getVersion());
        if (!entity.getCode().equalsIgnoreCase(command.code())) {
            rejectDuplicate(unitRepository.existsByBusinessIdAndCodeIgnoreCase(tenant(), command.code()), "Unit code");
        }
        mapper.update(command, entity);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public UnitOfMeasureResponse deactivateUnitOfMeasure(UUID id, long version) {
        UnitOfMeasure entity = requireUnitEntity(id);
        checkVersion("UnitOfMeasure", id, version, entity.getVersion());
        entity.setActive(false);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public EquipmentTypeReference requireEquipmentType(UUID id) {
        EquipmentType entity = requireEquipmentTypeEntity(id);
        return new EquipmentTypeReference(entity.getId(), entity.getCode(), entity.getName(), entity.getStatus());
    }

    @Override
    @Transactional(readOnly = true)
    public JobTypeReference requireJobType(UUID id) {
        JobType entity = requireJobTypeEntity(id);
        return new JobTypeReference(entity.getId(), entity.getCode(), entity.getName(), entity.getStatus());
    }

    @Override
    @Transactional(readOnly = true)
    public ChecklistTemplateReference requireChecklistTemplate(UUID id) {
        ChecklistTemplate entity = requireChecklistTemplateEntity(id);
        return new ChecklistTemplateReference(entity.getId(), entity.getEquipmentTypeId(), entity.getJobTypeId(), entity.getTemplateVersion(), entity.getStatus());
    }

    @Override
    @Transactional(readOnly = true)
    public MaintenanceTemplateReference requireMaintenanceTemplate(UUID id) {
        MaintenanceTemplate entity = requireMaintenanceTemplateEntity(id);
        return new MaintenanceTemplateReference(entity.getId(), entity.getEquipmentTypeId(), entity.getJobTypeId(), entity.getChecklistTemplateId(), entity.getTemplateVersion(), entity.getStatus());
    }

    private UUID tenant() {
        return tenantContext.requireBusinessId();
    }

    private EquipmentType requireEquipmentTypeEntity(UUID id) {
        return equipmentTypeRepository.findByIdAndBusinessId(id, tenant())
                .orElseThrow(() -> new EntityNotFoundException("EquipmentType", id));
    }

    private JobType requireJobTypeEntity(UUID id) {
        return jobTypeRepository.findByIdAndBusinessId(id, tenant())
                .orElseThrow(() -> new EntityNotFoundException("JobType", id));
    }

    private ChecklistTemplate requireChecklistTemplateEntity(UUID id) {
        return checklistTemplateRepository.findByIdAndBusinessId(id, tenant())
                .orElseThrow(() -> new EntityNotFoundException("ChecklistTemplate", id));
    }

    private MaintenanceTemplate requireMaintenanceTemplateEntity(UUID id) {
        return maintenanceTemplateRepository.findByIdAndBusinessId(id, tenant())
                .orElseThrow(() -> new EntityNotFoundException("MaintenanceTemplate", id));
    }

    private UnitOfMeasure requireUnitEntity(UUID id) {
        return unitRepository.findByIdAndBusinessId(id, tenant())
                .orElseThrow(() -> new EntityNotFoundException("UnitOfMeasure", id));
    }

    private static void checkVersion(String name, UUID id, long expected, long actual) {
        if (expected != actual) {
            throw new StaleEntityException(name, id, expected, actual);
        }
    }

    private static void rejectDuplicate(boolean duplicate, String subject) {
        if (duplicate) {
            throw new IllegalArgumentException(subject + " already exists in this tenant");
        }
    }

    private static void requireActive(String status, String entity) {
        if (!"ACTIVE".equals(status)) {
            throw new IllegalStateException(entity + " is not active");
        }
    }

    private static void requireNotArchived(String status, String entity) {
        if ("ARCHIVED".equals(status)) {
            throw new IllegalStateException(entity + " is archived");
        }
    }
}
