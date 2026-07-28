package com.esmpf.platform;

import static com.esmpf.platform.PlatformDtos.*;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PlatformService {
    PublicTokenResponse createPublicToken(PublicTokenCommand command);
    PublicTokenResponse consumePublicToken(UUID tokenId);
    PublicTokenResponse revokePublicToken(UUID tokenId, long version);

    DataJobResponse createDataJob(DataJobCommand command);
    DataJobResponse startDataJob(UUID jobId, long version);
    DataJobResponse updateDataJobProgress(UUID jobId, long version, int progress);
    DataJobResponse completeDataJob(UUID jobId, long version, UUID resultAttachmentId);
    DataJobResponse failDataJob(UUID jobId, long version, String errorsJson);
    Page<DataJobResponse> listDataJobs(Pageable pageable);

    OutboxResponse appendOutboxEvent(OutboxCommand command);
    OutboxResponse markOutboxPublishing(UUID eventId, long version);
    OutboxResponse markOutboxPublished(UUID eventId, long version);
    OutboxResponse markOutboxFailed(UUID eventId, long version, String error, Instant nextAttemptAt);
    Page<OutboxResponse> listOutbox(Pageable pageable);

    AuditResponse appendAudit(AuditCommand command);
    Page<AuditResponse> listAudit(Pageable pageable);

    IdempotencyResponse beginIdempotentOperation(IdempotencyCommand command);
    IdempotencyResponse completeIdempotentOperation(UUID recordId, long version, String responseReference);
    IdempotencyResponse failIdempotentOperation(UUID recordId, long version);

    IntegrationResponse createIntegration(IntegrationCommand command);
    IntegrationResponse getIntegration(UUID connectionId);
    IntegrationResponse updateIntegration(UUID connectionId, IntegrationCommand command);
    IntegrationResponse activateIntegration(UUID connectionId, long version);
    IntegrationResponse suspendIntegration(UUID connectionId, long version);
    IntegrationResponse recordIntegrationSuccess(UUID connectionId, long version);
    IntegrationResponse recordIntegrationFailure(UUID connectionId, long version);
    Page<IntegrationResponse> listIntegrations(Pageable pageable);

    String allocateDocumentNumber(String documentType, int year, String prefix);
}
