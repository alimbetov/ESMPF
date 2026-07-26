package com.esmpf.equipment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public final class EquipmentDtos {
    private EquipmentDtos() {
    }

    public record EquipmentCreateCommand(
            @NotNull UUID customerId,
            @NotNull UUID serviceLocationId,
            @NotNull UUID equipmentTypeId,
            UUID parentEquipmentId,
            @NotBlank String name,
            String manufacturer,
            String model,
            String serialNumber,
            String assetNumber,
            LocalDate installationDate,
            LocalDate commissioningDate,
            LocalDate warrantyUntil,
            String attributesJson,
            String currentMeterValuesJson
    ) {
    }

    public record EquipmentUpdateCommand(
            long version,
            UUID serviceLocationId,
            UUID parentEquipmentId,
            String name,
            String manufacturer,
            String model,
            String serialNumber,
            String assetNumber,
            LocalDate installationDate,
            LocalDate commissioningDate,
            LocalDate warrantyUntil,
            String attributesJson,
            String currentMeterValuesJson
    ) {
    }

    public record EquipmentResponse(
            UUID id, UUID customerId, UUID serviceLocationId, UUID equipmentTypeId,
            UUID parentEquipmentId, String name, String manufacturer, String model,
            String serialNumber, String assetNumber, String status,
            LocalDate installationDate, LocalDate commissioningDate, LocalDate warrantyUntil,
            String attributesJson, String currentMeterValuesJson,
            Instant createdAt, Instant updatedAt, long version
    ) {
    }

    public record EquipmentRelationCreateCommand(
            @NotNull UUID sourceEquipmentId,
            @NotNull UUID targetEquipmentId,
            @NotBlank String relationType,
            LocalDate validFrom,
            LocalDate validUntil,
            String description
    ) {
    }

    public record EquipmentRelationResponse(
            UUID id, UUID sourceEquipmentId, UUID targetEquipmentId, String relationType,
            LocalDate validFrom, LocalDate validUntil, String description,
            Instant createdAt, Instant updatedAt, long version
    ) {
    }

    public record EquipmentIssueCreateCommand(
            @NotNull UUID equipmentId,
            UUID detectedByJobId,
            @NotBlank String type,
            @NotBlank String severity,
            @NotBlank String description,
            LocalDate dueDate
    ) {
    }

    public record EquipmentIssueResponse(
            UUID id, UUID equipmentId, UUID detectedByJobId, String type, String severity,
            String status, String description, Instant detectedAt, LocalDate dueDate,
            UUID resolvedByJobId, Instant resolvedAt,
            Instant createdAt, Instant updatedAt, long version
    ) {
    }

    public record MeterReadingCommand(
            @NotNull UUID equipmentId,
            @NotBlank String meterCode,
            @NotNull BigDecimal readingValue,
            @NotBlank String unitCode,
            Instant recordedAt,
            String source
    ) {
    }

    public record MeterReadingResponse(
            UUID id, UUID equipmentId, String meterCode, BigDecimal readingValue,
            String unitCode, Instant recordedAt, UUID recordedBy, String source,
            Instant createdAt, Instant updatedAt, long version
    ) {
    }

    public record EquipmentReference(
            UUID id, UUID customerId, UUID serviceLocationId, UUID equipmentTypeId,
            String name, String status
    ) {
    }
}
