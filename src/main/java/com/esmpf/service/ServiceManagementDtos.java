package com.esmpf.service;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public final class ServiceManagementDtos {
    private ServiceManagementDtos() {
    }

    public record ServiceRequestCreateCommand(
            @NotNull UUID customerId,
            @NotNull UUID serviceLocationId,
            UUID equipmentId,
            @NotBlank String source,
            @NotBlank String priority,
            @NotBlank String summary,
            String description
    ) {
    }

    public record ServiceRequestResponse(
            UUID id, UUID customerId, UUID serviceLocationId, UUID equipmentId,
            String source, String priority, String summary, String description,
            String status, Instant requestedAt, UUID requestedBy,
            Instant createdAt, Instant updatedAt, long version
    ) {
    }

    public record ServiceJobCreateCommand(
            UUID requestId,
            UUID maintenanceOccurrenceId,
            @NotNull UUID customerId,
            @NotNull UUID serviceLocationId,
            UUID equipmentId,
            @NotNull UUID jobTypeId,
            UUID serviceAgreementId,
            @NotBlank String priority,
            @NotBlank String title,
            String description,
            UUID leadWorkerId,
            String assignedWorkerIdsJson
    ) {
    }

    public record ServiceJobScheduleCommand(
            long version,
            @NotNull Instant plannedStart,
            @NotNull Instant plannedEnd,
            UUID leadWorkerId,
            String assignedWorkerIdsJson
    ) {
    }

    public record ServiceJobResponse(
            UUID id, UUID requestId, UUID maintenanceOccurrenceId, UUID customerId,
            UUID serviceLocationId, UUID equipmentId, UUID jobTypeId,
            UUID serviceAgreementId, String status, String priority, String title,
            String description, Instant plannedStart, Instant plannedEnd,
            UUID leadWorkerId, String assignedWorkerIdsJson, String blockedReason,
            Instant createdAt, Instant updatedAt, long version
    ) {
    }

    public record JobVisitPlanCommand(
            @NotNull UUID jobId,
            @NotNull Instant scheduledStart,
            @NotNull Instant scheduledEnd,
            String workerIdsJson
    ) {
    }

    public record JobVisitResponse(
            UUID id, UUID jobId, Instant scheduledStart, Instant scheduledEnd,
            Instant actualStart, Instant actualEnd, String status,
            String workerIdsJson, String arrivalDataJson, String completionDataJson,
            String customerConfirmationJson, Instant createdAt, Instant updatedAt, long version
    ) {
    }

    public record JobExecutionStartCommand(
            @NotNull UUID jobId,
            @NotNull UUID visitId,
            @NotNull UUID checklistTemplateId,
            @NotNull Integer templateVersion,
            @NotBlank String schemaSnapshotJson
    ) {
    }

    public record JobExecutionResponse(
            UUID id, UUID jobId, UUID visitId, UUID checklistTemplateId,
            Integer templateVersion, String schemaSnapshotJson, String answersJson,
            Instant startedAt, Instant completedAt, UUID completedBy, String status,
            Instant createdAt, Instant updatedAt, long version
    ) {
    }

    public record WorkReportCreateCommand(
            @NotNull UUID jobId,
            UUID visitId,
            UUID jobExecutionId,
            @NotBlank String diagnosis,
            @NotBlank String workPerformed,
            @NotBlank String result,
            String materialsSummaryJson,
            String measurementsSummaryJson,
            String customerComment
    ) {
    }

    public record WorkReportResponse(
            UUID id, UUID jobId, UUID visitId, UUID jobExecutionId,
            String diagnosis, String workPerformed, String result,
            String materialsSummaryJson, String measurementsSummaryJson,
            String customerComment, UUID completedBy, Instant completedAt, String status,
            Instant createdAt, Instant updatedAt, long version
    ) {
    }

    public record ServiceJobReference(
            UUID id, UUID customerId, UUID serviceLocationId, UUID equipmentId,
            UUID jobTypeId, String status
    ) {
    }
}