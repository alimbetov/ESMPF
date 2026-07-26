package com.esmpf.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class CatalogDtos {
    private CatalogDtos() {
    }

    public record EquipmentTypeCommand(
            long version,
            @NotBlank String code,
            @NotBlank String name,
            String category,
            Integer schemaVersion,
            String attributeSchemaJson,
            String measurementSchemaJson,
            String meterSchemaJson
    ) {
    }

    public record EquipmentTypeResponse(
            UUID id, String code, String name, String category, Integer schemaVersion,
            String attributeSchemaJson, String measurementSchemaJson, String meterSchemaJson,
            String status, Instant createdAt, Instant updatedAt, long version
    ) {
    }

    public record JobTypeCommand(
            long version,
            @NotBlank String code,
            @NotBlank String name,
            String category,
            Integer defaultDurationMinutes,
            BigDecimal defaultPrice,
            Boolean requiresChecklist,
            Boolean requiresSignature,
            Boolean requiresPdfReport,
            String settingsJson
    ) {
    }

    public record JobTypeResponse(
            UUID id, String code, String name, String category, Integer defaultDurationMinutes,
            BigDecimal defaultPrice, Boolean requiresChecklist, Boolean requiresSignature,
            Boolean requiresPdfReport, String settingsJson, String status,
            Instant createdAt, Instant updatedAt, long version
    ) {
    }

    public record ChecklistTemplateCommand(
            long version,
            @NotBlank String code,
            @NotBlank String name,
            UUID equipmentTypeId,
            UUID jobTypeId,
            @NotNull Integer templateVersion,
            @NotBlank String schemaJson
    ) {
    }

    public record ChecklistTemplateResponse(
            UUID id, String code, String name, UUID equipmentTypeId, UUID jobTypeId,
            Integer templateVersion, String schemaJson, String status, Instant publishedAt,
            Instant createdAt, Instant updatedAt, long version
    ) {
    }

    public record MaintenanceTemplateCommand(
            long version,
            @NotBlank String code,
            @NotBlank String name,
            @NotNull UUID equipmentTypeId,
            @NotNull UUID jobTypeId,
            UUID checklistTemplateId,
            @NotNull Integer templateVersion,
            @NotBlank String scheduleRuleJson,
            String reminderRuleJson,
            String settingsJson
    ) {
    }

    public record MaintenanceTemplateResponse(
            UUID id, String code, String name, UUID equipmentTypeId, UUID jobTypeId,
            UUID checklistTemplateId, Integer templateVersion, String scheduleRuleJson,
            String reminderRuleJson, String settingsJson, String status,
            Instant createdAt, Instant updatedAt, long version
    ) {
    }

    public record UnitOfMeasureCommand(
            long version,
            @NotBlank String code,
            @NotBlank String symbol,
            @NotBlank String name,
            @NotBlank String quantityType,
            Integer precisionScale
    ) {
    }

    public record UnitOfMeasureResponse(
            UUID id, String code, String symbol, String name, String quantityType,
            Integer precisionScale, Boolean active, Instant createdAt, Instant updatedAt, long version
    ) {
    }

    public record EquipmentTypeReference(UUID id, String code, String name, String status) {
    }

    public record JobTypeReference(UUID id, String code, String name, String status) {
    }

    public record ChecklistTemplateReference(
            UUID id, UUID equipmentTypeId, UUID jobTypeId, Integer templateVersion, String status
    ) {
    }

    public record MaintenanceTemplateReference(
            UUID id, UUID equipmentTypeId, UUID jobTypeId, UUID checklistTemplateId,
            Integer templateVersion, String status
    ) {
    }
}
