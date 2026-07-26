package com.esmpf.maintenance;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public final class MaintenanceDtos {
    private MaintenanceDtos() {
    }

    public record MaintenancePlanCreateCommand(
            @NotNull UUID equipmentId,
            @NotNull UUID maintenanceTemplateId,
            LocalDate activeFrom,
            LocalDate activeUntil,
            LocalDate nextDueDate,
            BigDecimal nextDueMeterValue,
            String overridesJson
    ) {
    }

    public record MaintenancePlanUpdateCommand(
            long version,
            LocalDate activeFrom,
            LocalDate activeUntil,
            LocalDate nextDueDate,
            BigDecimal nextDueMeterValue,
            String overridesJson
    ) {
    }

    public record MaintenancePlanResponse(
            UUID id, UUID equipmentId, UUID maintenanceTemplateId, Integer templateVersion,
            LocalDate activeFrom, LocalDate activeUntil, LocalDate nextDueDate,
            BigDecimal nextDueMeterValue, Instant lastCompletedAt, String overridesJson,
            String status, Instant createdAt, Instant updatedAt, long version
    ) {
    }

    public record MaintenanceOccurrenceCreateCommand(
            @NotNull UUID maintenancePlanId,
            LocalDate dueDate,
            BigDecimal dueMeterValue,
            @NotNull String generationKey,
            String reason
    ) {
    }

    public record MaintenanceOccurrenceResponse(
            UUID id, UUID maintenancePlanId, LocalDate dueDate, BigDecimal dueMeterValue,
            String status, UUID serviceJobId, Instant generatedAt, Instant completedAt,
            String generationKey, String reason, Instant createdAt, Instant updatedAt, long version
    ) {
    }

    public record MaintenancePlanReference(
            UUID id, UUID equipmentId, UUID maintenanceTemplateId, String status
    ) {
    }

    public record MaintenanceOccurrenceReference(
            UUID id, UUID maintenancePlanId, UUID serviceJobId, String status
    ) {
    }
}
