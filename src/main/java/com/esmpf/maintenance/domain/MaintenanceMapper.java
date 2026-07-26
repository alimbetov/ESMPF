package com.esmpf.maintenance.domain;

import static com.esmpf.maintenance.MaintenanceDtos.*;

import com.esmpf.shared.mapping.EsmpfMapperConfig;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = EsmpfMapperConfig.class)
interface MaintenanceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "templateVersion", ignore = true)
    @Mapping(target = "lastCompletedAt", ignore = true)
    @Mapping(target = "status", constant = "DRAFT")
    MaintenancePlan toEntity(MaintenancePlanCreateCommand command);

    MaintenancePlanResponse toResponse(MaintenancePlan entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "equipmentId", ignore = true)
    @Mapping(target = "maintenanceTemplateId", ignore = true)
    @Mapping(target = "templateVersion", ignore = true)
    @Mapping(target = "lastCompletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "status", ignore = true)
    void update(MaintenancePlanUpdateCommand command, @MappingTarget MaintenancePlan entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "status", constant = "PLANNED")
    @Mapping(target = "serviceJobId", ignore = true)
    @Mapping(target = "generatedAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    MaintenanceOccurrence toEntity(MaintenanceOccurrenceCreateCommand command);

    MaintenanceOccurrenceResponse toResponse(MaintenanceOccurrence entity);
}
