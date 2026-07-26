package com.esmpf.platform.domain;

import static com.esmpf.platform.PlatformDtos.*;

import com.esmpf.document.DocumentReferenceQuery;
import com.esmpf.platform.PlatformService;
import com.esmpf.shared.exception.EntityNotFoundException;
import com.esmpf.shared.exception.StaleEntityException;
import com.esmpf.shared.tenant.TenantContext;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class PlatformServiceImpl implements PlatformService {
    private final TenantContext tenantContext;
    private final DocumentReferenceQuery documentReferences;
    private final PublicAccessTokenRepository tokenRepository;
    private final DataJobRepository dataJobRepository;
    private final OutboxEventRepository outboxRepository;
    private final AuditLogRepository auditRepository;
    private final IdempotencyRecordRepository idempotencyRepository;
    private final IntegrationConnectionRepository integrationRepository;
    private final DocumentSequenceRepository sequenceRepository;

    @Override @Transactional
    public PublicTokenResponse createPublicToken(PublicTokenCommand command) {
        if (command.expiresAt() == null || !command.expiresAt().isAfter(Instant.now())) throw new IllegalArgumentException("Token expiry must be in the future");
        if (command.maxUses() != null && command.maxUses() <= 0) throw new IllegalArgumentException("maxUses must be positive");
        if (tokenRepository.existsByBusinessIdAndTokenHash(tenant(), command.tokenHash())) throw new IllegalArgumentException("Token hash already exists");
        PublicAccessToken e = new PublicAccessToken(); e.setBusinessId(tenant()); e.setPurpose(command.purpose()); e.setSubjectType(command.subjectType()); e.setSubjectId(command.subjectId()); e.setTokenHash(command.tokenHash()); e.setExpiresAt(command.expiresAt()); e.setMaxUses(command.maxUses()); e.setUsedCount(0);
        return response(tokenRepository.saveAndFlush(e));
    }
    @Override @Transactional
    public PublicTokenResponse consumePublicToken(UUID id) {
        PublicAccessToken e = requireToken(id); if (e.getRevokedAt() != null) throw new IllegalStateException("Token revoked"); if (!e.getExpiresAt().isAfter(Instant.now())) throw new IllegalStateException("Token expired"); if (e.getMaxUses() != null && e.getUsedCount() >= e.getMaxUses()) throw new IllegalStateException("Token usage limit reached"); e.setUsedCount(e.getUsedCount() + 1); return response(tokenRepository.saveAndFlush(e));
    }
    @Override @Transactional public PublicTokenResponse revokePublicToken(UUID id, long version) { PublicAccessToken e = requireToken(id); checkVersion("PublicAccessToken", id, version, e.getVersion()); e.setRevokedAt(Instant.now()); return response(tokenRepository.saveAndFlush(e)); }

    @Override @Transactional
    public DataJobResponse createDataJob(DataJobCommand c) {
        if (c.sourceAttachmentId() != null) documentReferences.requireAttachment(c.sourceAttachmentId());
        DataJob e = new DataJob(); e.setBusinessId(tenant()); e.setType(c.type()); e.setFormat(c.format()); e.setSubjectType(c.subjectType()); e.setSourceAttachmentId(c.sourceAttachmentId()); e.setConfigurationJson(c.configurationJson()); e.setStatus("QUEUED"); e.setProgress(0); return response(dataJobRepository.saveAndFlush(e));
    }
    @Override @Transactional public DataJobResponse startDataJob(UUID id, long version) { DataJob e = requireDataJob(id); checkVersion("DataJob", id, version, e.getVersion()); requireStatus(e.getStatus(), "QUEUED"); e.setStatus("RUNNING"); return response(dataJobRepository.saveAndFlush(e)); }
    @Override @Transactional public DataJobResponse updateDataJobProgress(UUID id, long version, int progress) { DataJob e = requireDataJob(id); checkVersion("DataJob", id, version, e.getVersion()); requireStatus(e.getStatus(), "RUNNING"); if (progress < 0 || progress > 100) throw new IllegalArgumentException("progress must be 0..100"); e.setProgress(progress); return response(dataJobRepository.saveAndFlush(e)); }
    @Override @Transactional public DataJobResponse completeDataJob(UUID id, long version, UUID attachmentId) { DataJob e = requireDataJob(id); checkVersion("DataJob", id, version, e.getVersion()); requireStatus(e.getStatus(), "RUNNING"); documentReferences.requireAttachment(attachmentId); e.setResultAttachmentId(attachmentId); e.setProgress(100); e.setStatus("COMPLETED"); e.setCompletedAt(Instant.now()); return response(dataJobRepository.saveAndFlush(e)); }
    @Override @Transactional public DataJobResponse failDataJob(UUID id, long version, String errors) { DataJob e = requireDataJob(id); checkVersion("DataJob", id, version, e.getVersion()); requireStatus(e.getStatus(), "RUNNING"); e.setStatus("FAILED"); e.setErrorsJson(errors); e.setCompletedAt(Instant.now()); return response(dataJobRepository.saveAndFlush(e)); }
    @Override @Transactional(readOnly = true) public Page<DataJobResponse> listDataJobs(Pageable p) { return dataJobRepository.findAllByBusinessId(tenant(), p).map(this::response); }

    @Override @Transactional
    public OutboxResponse appendOutboxEvent(OutboxCommand c) {
        if (c.eventVersion() == null || c.eventVersion() <= 0) throw new IllegalArgumentException("eventVersion must be positive");
        OutboxEvent e = new OutboxEvent(); e.setBusinessId(tenant()); e.setAggregateType(c.aggregateType()); e.setAggregateId(c.aggregateId()); e.setEventType(c.eventType()); e.setEventVersion(c.eventVersion()); e.setPayloadJson(c.payloadJson()); e.setStatus("PENDING"); e.setAttemptCount(0); e.setNextAttemptAt(Instant.now()); return response(outboxRepository.saveAndFlush(e));
    }
    @Override @Transactional public OutboxResponse markOutboxPublishing(UUID id, long version) { OutboxEvent e = requireOutbox(id); checkVersion("OutboxEvent", id, version, e.getVersion()); if (!("PENDING".equals(e.getStatus()) || "FAILED".equals(e.getStatus()))) throw new IllegalStateException("Outbox event not publishable"); e.setStatus("PUBLISHING"); e.setAttemptCount(e.getAttemptCount() + 1); e.setLastError(null); return response(outboxRepository.saveAndFlush(e)); }
    @Override @Transactional public OutboxResponse markOutboxPublished(UUID id, long version) { OutboxEvent e = requireOutbox(id); checkVersion("OutboxEvent", id, version, e.getVersion()); requireStatus(e.getStatus(), "PUBLISHING"); e.setStatus("PUBLISHED"); e.setPublishedAt(Instant.now()); e.setNextAttemptAt(null); return response(outboxRepository.saveAndFlush(e)); }
    @Override @Transactional public OutboxResponse markOutboxFailed(UUID id, long version, String error, Instant next) { OutboxEvent e = requireOutbox(id); checkVersion("OutboxEvent", id, version, e.getVersion()); requireStatus(e.getStatus(), "PUBLISHING"); e.setStatus("FAILED"); e.setLastError(error); e.setNextAttemptAt(next); return response(outboxRepository.saveAndFlush(e)); }
    @Override @Transactional(readOnly = true) public Page<OutboxResponse> listOutbox(Pageable p) { return outboxRepository.findAllByBusinessId(tenant(), p).map(this::response); }

    @Override @Transactional
    public AuditResponse appendAudit(AuditCommand c) { AuditLog e = new AuditLog(); e.setBusinessId(tenant()); e.setActorType(c.actorType()); e.setActorId(c.actorId()); e.setAction(c.action()); e.setSubjectType(c.subjectType()); e.setSubjectId(c.subjectId()); e.setBeforeDataJson(c.beforeDataJson()); e.setAfterDataJson(c.afterDataJson()); e.setMetadataJson(c.metadataJson()); e.setOccurredAt(Instant.now()); return response(auditRepository.saveAndFlush(e)); }
    @Override @Transactional(readOnly = true) public Page<AuditResponse> listAudit(Pageable p) { return auditRepository.findAllByBusinessId(tenant(), p).map(this::response); }

    @Override @Transactional
    public IdempotencyResponse beginIdempotentOperation(IdempotencyCommand c) {
        var existing = idempotencyRepository.findByBusinessIdAndIdempotencyKeyAndOperation(tenant(), c.key(), c.operation());
        if (existing.isPresent()) { IdempotencyRecord e = existing.get(); if (!e.getRequestHash().equals(c.requestHash())) throw new IllegalArgumentException("Idempotency key reused with different request"); return response(e); }
        if (c.expiresAt() == null || !c.expiresAt().isAfter(Instant.now())) throw new IllegalArgumentException("Idempotency expiry must be in future");
        IdempotencyRecord e = new IdempotencyRecord(); e.setBusinessId(tenant()); e.setIdempotencyKey(c.key()); e.setOperation(c.operation()); e.setRequestHash(c.requestHash()); e.setStatus("STARTED"); e.setExpiresAt(c.expiresAt()); return response(idempotencyRepository.saveAndFlush(e));
    }
    @Override @Transactional public IdempotencyResponse completeIdempotentOperation(UUID id, long version, String ref) { IdempotencyRecord e = requireIdempotency(id); checkVersion("IdempotencyRecord", id, version, e.getVersion()); requireStatus(e.getStatus(), "STARTED"); e.setResponseReference(ref); e.setStatus("COMPLETED"); return response(idempotencyRepository.saveAndFlush(e)); }
    @Override @Transactional public IdempotencyResponse failIdempotentOperation(UUID id, long version) { IdempotencyRecord e = requireIdempotency(id); checkVersion("IdempotencyRecord", id, version, e.getVersion()); requireStatus(e.getStatus(), "STARTED"); e.setStatus("FAILED"); return response(idempotencyRepository.saveAndFlush(e)); }

    @Override @Transactional
    public IntegrationResponse createIntegration(IntegrationCommand c) { if (integrationRepository.existsByBusinessIdAndNameIgnoreCase(tenant(), c.name())) throw new IllegalArgumentException("Integration name already exists"); IntegrationConnection e = new IntegrationConnection(); e.setBusinessId(tenant()); apply(c, e); e.setStatus("INACTIVE"); return response(integrationRepository.saveAndFlush(e)); }
    @Override @Transactional public IntegrationResponse updateIntegration(UUID id, IntegrationCommand c) { IntegrationConnection e = requireIntegration(id); checkVersion("IntegrationConnection", id, c.version(), e.getVersion()); if ("ACTIVE".equals(e.getStatus())) throw new IllegalStateException("Active integration must be suspended before update"); apply(c, e); return response(integrationRepository.saveAndFlush(e)); }
    @Override @Transactional public IntegrationResponse activateIntegration(UUID id, long version) { return transitionIntegration(id, version, "ACTIVE"); }
    @Override @Transactional public IntegrationResponse suspendIntegration(UUID id, long version) { return transitionIntegration(id, version, "SUSPENDED"); }
    @Override @Transactional public IntegrationResponse recordIntegrationSuccess(UUID id, long version) { IntegrationConnection e = requireIntegration(id); checkVersion("IntegrationConnection", id, version, e.getVersion()); e.setLastSuccessfulAt(Instant.now()); return response(integrationRepository.saveAndFlush(e)); }
    @Override @Transactional public IntegrationResponse recordIntegrationFailure(UUID id, long version) { IntegrationConnection e = requireIntegration(id); checkVersion("IntegrationConnection", id, version, e.getVersion()); e.setLastErrorAt(Instant.now()); return response(integrationRepository.saveAndFlush(e)); }
    @Override @Transactional(readOnly = true) public Page<IntegrationResponse> listIntegrations(Pageable p) { return integrationRepository.findAllByBusinessId(tenant(), p).map(this::response); }

    @Override @Transactional
    public String allocateDocumentNumber(String type, int year, String prefix) {
        if (year < 2000 || year > 9999) throw new IllegalArgumentException("Invalid sequence year");
        DocumentSequence e = sequenceRepository.findByBusinessIdAndDocumentTypeAndSequenceYear(tenant(), type, year).orElseGet(() -> { DocumentSequence n = new DocumentSequence(); n.setBusinessId(tenant()); n.setDocumentType(type); n.setSequenceYear(year); n.setPrefix(prefix); n.setCurrentValue(0L); return n; });
        e.setCurrentValue(e.getCurrentValue() + 1); e = sequenceRepository.saveAndFlush(e); String effectivePrefix = e.getPrefix() == null ? "" : e.getPrefix(); return effectivePrefix + year + "-" + String.format("%06d", e.getCurrentValue());
    }

    private static void apply(IntegrationCommand c, IntegrationConnection e) { e.setType(c.type()); e.setName(c.name()); e.setConfigurationJson(c.configurationJson()); e.setSecretReference(c.secretReference()); }
    private IntegrationResponse transitionIntegration(UUID id, long version, String status) { IntegrationConnection e = requireIntegration(id); checkVersion("IntegrationConnection", id, version, e.getVersion()); e.setStatus(status); return response(integrationRepository.saveAndFlush(e)); }
    private PublicAccessToken requireToken(UUID id) { return tokenRepository.findByIdAndBusinessId(id, tenant()).orElseThrow(() -> new EntityNotFoundException("PublicAccessToken", id)); }
    private DataJob requireDataJob(UUID id) { return dataJobRepository.findByIdAndBusinessId(id, tenant()).orElseThrow(() -> new EntityNotFoundException("DataJob", id)); }
    private OutboxEvent requireOutbox(UUID id) { return outboxRepository.findByIdAndBusinessId(id, tenant()).orElseThrow(() -> new EntityNotFoundException("OutboxEvent", id)); }
    private IdempotencyRecord requireIdempotency(UUID id) { return idempotencyRepository.findByIdAndBusinessId(id, tenant()).orElseThrow(() -> new EntityNotFoundException("IdempotencyRecord", id)); }
    private IntegrationConnection requireIntegration(UUID id) { return integrationRepository.findByIdAndBusinessId(id, tenant()).orElseThrow(() -> new EntityNotFoundException("IntegrationConnection", id)); }
    private UUID tenant() { return tenantContext.requireBusinessId(); }
    private static void requireStatus(String actual, String expected) { if (!expected.equals(actual)) throw new IllegalStateException("Expected status " + expected + " but was " + actual); }
    private static void checkVersion(String type, UUID id, long expected, long actual) { if (expected != actual) throw new StaleEntityException(type, id, expected, actual); }
    private static PublicTokenResponse response(PublicAccessToken e) { return new PublicTokenResponse(e.getId(), e.getVersion(), e.getPurpose(), e.getSubjectType(), e.getSubjectId(), e.getExpiresAt(), e.getMaxUses(), e.getUsedCount(), e.getRevokedAt(), e.getCreatedAt(), e.getUpdatedAt()); }
    private static DataJobResponse response(DataJob e) { return new DataJobResponse(e.getId(), e.getVersion(), e.getType(), e.getFormat(), e.getSubjectType(), e.getStatus(), e.getSourceAttachmentId(), e.getResultAttachmentId(), e.getConfigurationJson(), e.getProgress(), e.getErrorsJson(), e.getCompletedAt(), e.getCreatedAt(), e.getUpdatedAt()); }
    private static OutboxResponse response(OutboxEvent e) { return new OutboxResponse(e.getId(), e.getVersion(), e.getAggregateType(), e.getAggregateId(), e.getEventType(), e.getEventVersion(), e.getPayloadJson(), e.getStatus(), e.getPublishedAt(), e.getAttemptCount(), e.getNextAttemptAt(), e.getLastError(), e.getCreatedAt(), e.getUpdatedAt()); }
    private static AuditResponse response(AuditLog e) { return new AuditResponse(e.getId(), e.getActorType(), e.getActorId(), e.getAction(), e.getSubjectType(), e.getSubjectId(), e.getBeforeDataJson(), e.getAfterDataJson(), e.getMetadataJson(), e.getOccurredAt()); }
    private static IdempotencyResponse response(IdempotencyRecord e) { return new IdempotencyResponse(e.getId(), e.getVersion(), e.getIdempotencyKey(), e.getOperation(), e.getRequestHash(), e.getResponseReference(), e.getStatus(), e.getExpiresAt()); }
    private static IntegrationResponse response(IntegrationConnection e) { return new IntegrationResponse(e.getId(), e.getVersion(), e.getType(), e.getName(), e.getStatus(), e.getConfigurationJson(), e.getSecretReference(), e.getLastSuccessfulAt(), e.getLastErrorAt(), e.getCreatedAt(), e.getUpdatedAt()); }
}