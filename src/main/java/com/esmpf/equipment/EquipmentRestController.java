package com.esmpf.equipment;

import static com.esmpf.equipment.EquipmentDtos.*;
import static com.esmpf.web.ApiActionRequests.ReferenceRequest;
import static com.esmpf.web.ApiActionRequests.VersionRequest;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class EquipmentRestController {
    private final EquipmentService service;

    @PostMapping("/equipment") @ResponseStatus(HttpStatus.CREATED)
    public EquipmentResponse createEquipment(@Valid @RequestBody EquipmentCreateCommand command) { return service.createEquipment(command); }
    @GetMapping("/equipment/{id}") public EquipmentResponse getEquipment(@PathVariable UUID id) { return service.getEquipment(id); }
    @GetMapping("/equipment") public Page<EquipmentResponse> listEquipment(Pageable pageable) { return service.listEquipment(pageable); }
    @PutMapping("/equipment/{id}") public EquipmentResponse updateEquipment(@PathVariable UUID id, @Valid @RequestBody EquipmentUpdateCommand command) { return service.updateEquipment(id, command); }
    @PostMapping("/equipment/{id}/actions/archive") public EquipmentResponse archiveEquipment(@PathVariable UUID id, @Valid @RequestBody VersionRequest request) { return service.archiveEquipment(id, request.version()); }

    @PostMapping("/equipment-relations") @ResponseStatus(HttpStatus.CREATED)
    public EquipmentRelationResponse createRelation(@Valid @RequestBody EquipmentRelationCreateCommand command) { return service.createRelation(command); }
    @PostMapping("/equipment-relations/{relationId}/actions/end") public EquipmentRelationResponse endRelation(@PathVariable UUID relationId, @Valid @RequestBody VersionRequest request) { return service.endRelation(relationId, request.version()); }
    @GetMapping("/equipment/{equipmentId}/relations") public Page<EquipmentRelationResponse> listRelations(@PathVariable UUID equipmentId, Pageable pageable) { return service.listRelations(equipmentId, pageable); }

    @PostMapping("/equipment-issues") @ResponseStatus(HttpStatus.CREATED)
    public EquipmentIssueResponse reportIssue(@Valid @RequestBody EquipmentIssueCreateCommand command) { return service.reportIssue(command); }
    @PostMapping("/equipment-issues/{issueId}/actions/resolve") public EquipmentIssueResponse resolveIssue(@PathVariable UUID issueId, @Valid @RequestBody ReferenceRequest request) { return service.resolveIssue(issueId, request.version(), request.referenceId()); }
    @GetMapping("/equipment/{equipmentId}/issues") public Page<EquipmentIssueResponse> listIssues(@PathVariable UUID equipmentId, Pageable pageable) { return service.listIssues(equipmentId, pageable); }

    @PostMapping("/meter-readings") @ResponseStatus(HttpStatus.CREATED)
    public MeterReadingResponse recordMeterReading(@Valid @RequestBody MeterReadingCommand command) { return service.recordMeterReading(command); }
    @GetMapping("/equipment/{equipmentId}/meter-readings") public Page<MeterReadingResponse> listMeterReadings(@PathVariable UUID equipmentId, Pageable pageable) { return service.listMeterReadings(equipmentId, pageable); }
}
