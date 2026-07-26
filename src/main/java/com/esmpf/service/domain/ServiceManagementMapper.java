package com.esmpf.service.domain;

import static com.esmpf.service.ServiceManagementDtos.*;

import com.esmpf.shared.mapping.EsmpfMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = EsmpfMapperConfig.class)
interface ServiceManagementMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "status", constant = "NEW")
    @Mapping(target = "requestedAt", ignore = true)
    @Mapping(target = "requestedBy", ignore = true)
    ServiceRequest toEntity(ServiceRequestCreateCommand command);

    ServiceRequestResponse toResponse(ServiceRequest entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "status", constant = "DRAFT")
    @Mapping(target = "plannedStart", ignore = true)
    @Mapping(target = "plannedEnd", ignore = true)
    @Mapping(target = "blockedReason", ignore = true)
    ServiceJob toEntity(ServiceJobCreateCommand command);

    ServiceJobResponse toResponse(ServiceJob entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "actualStart", ignore = true)
    @Mapping(target = "actualEnd", ignore = true)
    @Mapping(target = "status", constant = "PLANNED")
    @Mapping(target = "arrivalDataJson", ignore = true)
    @Mapping(target = "completionDataJson", ignore = true)
    @Mapping(target = "customerConfirmationJson", ignore = true)
    JobVisit toEntity(JobVisitPlanCommand command);

    JobVisitResponse toResponse(JobVisit entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "answersJson", ignore = true)
    @Mapping(target = "startedAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "completedBy", ignore = true)
    @Mapping(target = "status", constant = "IN_PROGRESS")
    JobExecution toEntity(JobExecutionStartCommand command);

    JobExecutionResponse toResponse(JobExecution entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "completedBy", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "status", constant = "DRAFT")
    WorkReport toEntity(WorkReportCreateCommand command);

    WorkReportResponse toResponse(WorkReport entity);
}
