package com.esmpf.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public final class ServiceSupportDtos {
    private ServiceSupportDtos() {}
    public record RecommendationCommand(UUID equipmentId, UUID sourceJobId, String description,
                                        String priority, LocalDate dueDate) {}
    public record RecommendationResponse(UUID id, long version, UUID equipmentId, UUID sourceJobId,
                                         String description, String priority, LocalDate dueDate,
                                         String status, UUID convertedJobId,
                                         Instant createdAt, Instant updatedAt) {}

    public record MaterialCatalogCommand(long version, String code, String name, String unitCode,
                                         BigDecimal defaultPrice, String currency) {}
    public record MaterialCatalogResponse(UUID id, long version, String code, String name,
                                          String unitCode, BigDecimal defaultPrice, String currency,
                                          boolean active, Instant createdAt, Instant updatedAt) {}

    public record JobMaterialCommand(UUID jobId, UUID materialCatalogItemId, String type,
                                     String description, BigDecimal quantity, String unitCode,
                                     BigDecimal unitPrice, String currency, String source) {}
    public record JobMaterialResponse(UUID id, long version, UUID jobId, UUID materialCatalogItemId,
                                      String type, String description, BigDecimal quantity,
                                      String unitCode, BigDecimal unitPrice, String currency,
                                      String source, Instant createdAt, Instant updatedAt) {}

    public record ServiceAgreementCommand(long version, UUID customerId, String number, String type,
                                          LocalDate validFrom, LocalDate validUntil,
                                          String coveredEquipmentIdsJson, String coverageRulesJson,
                                          String slaRulesJson, String pricingRulesJson,
                                          UUID attachmentId) {}
    public record ServiceAgreementResponse(UUID id, long version, UUID customerId, String number,
                                           String type, String status, LocalDate validFrom,
                                           LocalDate validUntil, String coveredEquipmentIdsJson,
                                           String coverageRulesJson, String slaRulesJson,
                                           String pricingRulesJson, UUID attachmentId,
                                           Instant createdAt, Instant updatedAt) {}

    public record WarrantyCaseCommand(UUID equipmentId, UUID jobId, String source, String description) {}
    public record WarrantyCaseResponse(UUID id, long version, UUID equipmentId, UUID jobId,
                                       String source, String status, String description,
                                       String decision, Instant openedAt, Instant resolvedAt,
                                       Instant createdAt, Instant updatedAt) {}

    public record MobileDeviceCommand(UUID userId, String deviceIdentifier, String platform,
                                      String appVersion) {}
    public record MobileDeviceResponse(UUID id, long version, UUID userId, String deviceIdentifier,
                                       String platform, String appVersion, String status,
                                       Instant lastSeenAt, Instant registeredAt,
                                       Instant createdAt, Instant updatedAt) {}

    public record SyncOperationCommand(UUID deviceId, String clientOperationId, String operationType,
                                       String subjectType, UUID subjectId, String payloadHash,
                                       Instant occurredAt) {}
    public record SyncOperationResponse(UUID id, long version, UUID deviceId,
                                        String clientOperationId, String operationType,
                                        String subjectType, UUID subjectId, String payloadHash,
                                        String status, Instant occurredAt, Instant receivedAt,
                                        String errorCode, Instant createdAt, Instant updatedAt) {}
}