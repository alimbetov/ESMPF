package com.esmpf.communication;

import java.time.Instant;
import java.util.UUID;

public final class CommunicationDtos {
    private CommunicationDtos() {}
    public record NotificationTemplateCommand(long version, String code, String channel, String locale,
                                              Integer templateVersion, String subjectTemplate,
                                              String bodyTemplate) {}
    public record NotificationTemplateResponse(UUID id, long version, String code, String channel,
                                               String locale, Integer templateVersion,
                                               String subjectTemplate, String bodyTemplate,
                                               String status, Instant createdAt, Instant updatedAt) {}
    public record NotificationCommand(UUID customerId, String recipient, String channel,
                                      UUID notificationTemplateId, String payloadJson,
                                      Instant nextAttemptAt) {}
    public record NotificationResponse(UUID id, long version, UUID customerId, String recipient,
                                       String channel, UUID notificationTemplateId, String payloadJson,
                                       String status, Integer attemptCount, Instant nextAttemptAt,
                                       Instant sentAt, String providerMessageId, String lastError,
                                       Instant createdAt, Instant updatedAt) {}
    public record FeedbackCommand(UUID customerId, UUID jobId, String type, Integer rating,
                                  String comment, boolean publicationConsent) {}
    public record FeedbackResponse(UUID id, long version, UUID customerId, UUID jobId, String type,
                                   Integer rating, String comment, boolean publicationConsent,
                                   String status, String companyResponse, UUID respondedBy,
                                   Instant respondedAt, Instant resolvedAt,
                                   Instant createdAt, Instant updatedAt) {}
}