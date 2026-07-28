package com.esmpf.catalog;

import static com.esmpf.catalog.CatalogDtos.*;
import static com.esmpf.web.ApiActionRequests.VersionRequest;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/catalog")
@RequiredArgsConstructor
public class CatalogRestController {
    private final CatalogService service;

    @PostMapping("/equipment-types") @ResponseStatus(HttpStatus.CREATED)
    public EquipmentTypeResponse createEquipmentType(@Valid @RequestBody EquipmentTypeCommand command) { return service.createEquipmentType(command); }
    @GetMapping("/equipment-types/{id}") public EquipmentTypeResponse getEquipmentType(@PathVariable UUID id) { return service.getEquipmentType(id); }
    @GetMapping("/equipment-types") public Page<EquipmentTypeResponse> listEquipmentTypes(Pageable pageable) { return service.listEquipmentTypes(pageable); }
    @PutMapping("/equipment-types/{id}") public EquipmentTypeResponse updateEquipmentType(@PathVariable UUID id, @Valid @RequestBody EquipmentTypeCommand command) { return service.updateEquipmentType(id, command); }
    @PostMapping("/equipment-types/{id}/actions/archive") public EquipmentTypeResponse archiveEquipmentType(@PathVariable UUID id, @Valid @RequestBody VersionRequest request) { return service.archiveEquipmentType(id, request.version()); }

    @PostMapping("/job-types") @ResponseStatus(HttpStatus.CREATED)
    public JobTypeResponse createJobType(@Valid @RequestBody JobTypeCommand command) { return service.createJobType(command); }
    @GetMapping("/job-types/{id}") public JobTypeResponse getJobType(@PathVariable UUID id) { return service.getJobType(id); }
    @GetMapping("/job-types") public Page<JobTypeResponse> listJobTypes(Pageable pageable) { return service.listJobTypes(pageable); }
    @PutMapping("/job-types/{id}") public JobTypeResponse updateJobType(@PathVariable UUID id, @Valid @RequestBody JobTypeCommand command) { return service.updateJobType(id, command); }
    @PostMapping("/job-types/{id}/actions/archive") public JobTypeResponse archiveJobType(@PathVariable UUID id, @Valid @RequestBody VersionRequest request) { return service.archiveJobType(id, request.version()); }

    @PostMapping("/checklist-templates") @ResponseStatus(HttpStatus.CREATED)
    public ChecklistTemplateResponse createChecklistTemplate(@Valid @RequestBody ChecklistTemplateCommand command) { return service.createChecklistTemplate(command); }
    @PostMapping("/checklist-templates/{id}/actions/publish") public ChecklistTemplateResponse publishChecklistTemplate(@PathVariable UUID id, @Valid @RequestBody VersionRequest request) { return service.publishChecklistTemplate(id, request.version()); }
    @GetMapping("/checklist-templates/{id}") public ChecklistTemplateResponse getChecklistTemplate(@PathVariable UUID id) { return service.getChecklistTemplate(id); }
    @GetMapping("/checklist-templates") public Page<ChecklistTemplateResponse> listChecklistTemplates(Pageable pageable) { return service.listChecklistTemplates(pageable); }

    @PostMapping("/maintenance-templates") @ResponseStatus(HttpStatus.CREATED)
    public MaintenanceTemplateResponse createMaintenanceTemplate(@Valid @RequestBody MaintenanceTemplateCommand command) { return service.createMaintenanceTemplate(command); }
    @GetMapping("/maintenance-templates/{id}") public MaintenanceTemplateResponse getMaintenanceTemplate(@PathVariable UUID id) { return service.getMaintenanceTemplate(id); }
    @GetMapping("/maintenance-templates") public Page<MaintenanceTemplateResponse> listMaintenanceTemplates(Pageable pageable) { return service.listMaintenanceTemplates(pageable); }
    @PostMapping("/maintenance-templates/{id}/actions/archive") public MaintenanceTemplateResponse archiveMaintenanceTemplate(@PathVariable UUID id, @Valid @RequestBody VersionRequest request) { return service.archiveMaintenanceTemplate(id, request.version()); }

    @PostMapping("/units-of-measure") @ResponseStatus(HttpStatus.CREATED)
    public UnitOfMeasureResponse createUnitOfMeasure(@Valid @RequestBody UnitOfMeasureCommand command) { return service.createUnitOfMeasure(command); }
    @GetMapping("/units-of-measure/{id}") public UnitOfMeasureResponse getUnitOfMeasure(@PathVariable UUID id) { return service.getUnitOfMeasure(id); }
    @GetMapping("/units-of-measure") public Page<UnitOfMeasureResponse> listUnitsOfMeasure(Pageable pageable) { return service.listUnitsOfMeasure(pageable); }
    @PutMapping("/units-of-measure/{id}") public UnitOfMeasureResponse updateUnitOfMeasure(@PathVariable UUID id, @Valid @RequestBody UnitOfMeasureCommand command) { return service.updateUnitOfMeasure(id, command); }
    @PostMapping("/units-of-measure/{id}/actions/deactivate") public UnitOfMeasureResponse deactivateUnitOfMeasure(@PathVariable UUID id, @Valid @RequestBody VersionRequest request) { return service.deactivateUnitOfMeasure(id, request.version()); }
}
