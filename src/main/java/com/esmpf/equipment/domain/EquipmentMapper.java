package com.esmpf.equipment.domain;

import static com.esmpf.equipment.EquipmentDtos.*;

import com.esmpf.shared.mapping.EsmpfMapperConfig;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = EsmpfMapperConfig.class)
interface EquipmentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    Equipment toEntity(EquipmentCreateCommand command);

    EquipmentResponse toResponse(Equipment entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "customerId", ignore = true)
    @Mapping(target = "equipmentTypeId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "status", ignore = true)
    void update(EquipmentUpdateCommand command, @MappingTarget Equipment entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    EquipmentRelation toEntity(EquipmentRelationCreateCommand command);

    EquipmentRelationResponse toResponse(EquipmentRelation entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "status", constant = "OPEN")
    @Mapping(target = "detectedAt", ignore = true)
    @Mapping(target = "resolvedByJobId", ignore = true)
    @Mapping(target = "resolvedAt", ignore = true)
    EquipmentIssue toEntity(EquipmentIssueCreateCommand command);

    EquipmentIssueResponse toResponse(EquipmentIssue entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "recordedBy", ignore = true)
    MeterReading toEntity(MeterReadingCommand command);

    MeterReadingResponse toResponse(MeterReading entity);
}
