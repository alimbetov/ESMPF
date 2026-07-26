package com.esmpf.equipment;

import static com.esmpf.equipment.EquipmentDtos.*;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EquipmentService {
    EquipmentResponse createEquipment(EquipmentCreateCommand command);
    EquipmentResponse getEquipment(UUID id);
    Page<EquipmentResponse> listEquipment(Pageable pageable);
    EquipmentResponse updateEquipment(UUID id, EquipmentUpdateCommand command);
    EquipmentResponse archiveEquipment(UUID id, long version);

    EquipmentRelationResponse createRelation(EquipmentRelationCreateCommand command);
    EquipmentRelationResponse endRelation(UUID relationId, long version);
    Page<EquipmentRelationResponse> listRelations(UUID equipmentId, Pageable pageable);

    EquipmentIssueResponse reportIssue(EquipmentIssueCreateCommand command);
    EquipmentIssueResponse resolveIssue(UUID issueId, long version, UUID resolvedByJobId);
    Page<EquipmentIssueResponse> listIssues(UUID equipmentId, Pageable pageable);

    MeterReadingResponse recordMeterReading(MeterReadingCommand command);
    Page<MeterReadingResponse> listMeterReadings(UUID equipmentId, Pageable pageable);
}
