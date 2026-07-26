package com.esmpf.catalog;

import static com.esmpf.catalog.CatalogDtos.*;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CatalogService {
    EquipmentTypeResponse createEquipmentType(EquipmentTypeCommand command);
    EquipmentTypeResponse getEquipmentType(UUID id);
    Page<EquipmentTypeResponse> listEquipmentTypes(Pageable pageable);
    EquipmentTypeResponse updateEquipmentType(UUID id, EquipmentTypeCommand command);
    EquipmentTypeResponse archiveEquipmentType(UUID id, long version);

    JobTypeResponse createJobType(JobTypeCommand command);
    JobTypeResponse getJobType(UUID id);
    Page<JobTypeResponse> listJobTypes(Pageable pageable);
    JobTypeResponse updateJobType(UUID id, JobTypeCommand command);
    JobTypeResponse archiveJobType(UUID id, long version);

    ChecklistTemplateResponse createChecklistTemplate(ChecklistTemplateCommand command);
    ChecklistTemplateResponse publishChecklistTemplate(UUID id, long version);
    ChecklistTemplateResponse getChecklistTemplate(UUID id);
    Page<ChecklistTemplateResponse> listChecklistTemplates(Pageable pageable);

    MaintenanceTemplateResponse createMaintenanceTemplate(MaintenanceTemplateCommand command);
    MaintenanceTemplateResponse getMaintenanceTemplate(UUID id);
    Page<MaintenanceTemplateResponse> listMaintenanceTemplates(Pageable pageable);
    MaintenanceTemplateResponse archiveMaintenanceTemplate(UUID id, long version);

    UnitOfMeasureResponse createUnitOfMeasure(UnitOfMeasureCommand command);
    UnitOfMeasureResponse getUnitOfMeasure(UUID id);
    Page<UnitOfMeasureResponse> listUnitsOfMeasure(Pageable pageable);
    UnitOfMeasureResponse updateUnitOfMeasure(UUID id, UnitOfMeasureCommand command);
    UnitOfMeasureResponse deactivateUnitOfMeasure(UUID id, long version);
}
