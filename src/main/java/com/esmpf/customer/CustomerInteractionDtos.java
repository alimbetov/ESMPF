package com.esmpf.customer;

import java.time.Instant;
import java.util.UUID;

public final class CustomerInteractionDtos {
    private CustomerInteractionDtos() {}
    public record CustomerInteractionCommand(UUID customerId, String type, String subject,
                                             String content, Instant occurredAt,
                                             String relatedSubjectType, UUID relatedSubjectId) {}
    public record CustomerInteractionResponse(UUID id, long version, UUID customerId,
                                              String type, String subject, String content,
                                              Instant occurredAt, UUID createdBy,
                                              String relatedSubjectType, UUID relatedSubjectId,
                                              Instant createdAt, Instant updatedAt) {}
}