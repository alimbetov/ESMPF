package com.esmpf.platform;

import java.time.Instant;
import java.util.UUID;

public final class PlatformDtos {
    private PlatformDtos() {}
    public record PublicTokenCommand(String purpose, String subjectType, UUID subjectId,
                                     String tokenHash, Instant expiresAt, Integer maxUses) {}
    public record PublicTokenResponse(UUID id, long version, String purpose, String subjectType,
                                      UUID subjectId, Instant expiresAt, Integer maxUses,
                                      Integer usedCount, Instant revokedAt,
                                      Instant createdAt, Instant updatedAt) {}
    public record DataJobCommand(String type, String format, String subjectType,
                                 UUID sourceAttachmentId, String configurationJson) {}
    public record DataJobResponse(UUID id, long version, String type, String format,
                                  String subjectType, String status, UUID sourceAttachmentId,
                                  UUID resultAttachmentId, String configurationJson,
                                  Integer progress, String errorsJson, Instant completedAt,
                                  Instant createdAt, Instant updatedAt) {}
    public record OutboxCommand(String aggregateType, UUID aggregateId, String eventType,
                                Integer eventVersion, String payloadJson) {}
    public record OutboxResponse(UUID id, long version, String aggregateType, UUID aggregateId,
                                 String eventType, Integer eventVersion, String payloadJson,
                                 String status, Instant publishedAt, Integer attemptCount,
                                 Instant nextAttemptAt, String lastError,
                                 Instant createdAt, Instant updatedAt) {}
    public record AuditCommand(String actorType, UUID actorId, String action, String subjectType,
                               UUID subjectId, String beforeDataJson, String afterDataJson,
                               String metadataJson) {}
    public record AuditResponse(UUID id, String actorType, UUID actorId, String action,
                                String subjectType, UUID subjectId, String beforeDataJson,
                                String afterDataJson, String metadataJson, Instant occurredAt) {}
    public record IdempotencyCommand(String key, String operation, String requestHash,
                                     Instant expiresAt) {}
    public record IdempotencyResponse(UUID id, long version, String key, String operation,
                                      String requestHash, String responseReference,
                                      String status, Instant expiresAt) {}
    public record IntegrationCommand(long version, String type, String name,
                                     String configurationJson, String secretReference) {}
    public record IntegrationResponse(UUID id, long version, String type, String name,
                                      String status, String configurationJson,
                                      String secretReference, Instant lastSuccessfulAt,
                                      Instant lastErrorAt, Instant createdAt, Instant updatedAt) {}
}