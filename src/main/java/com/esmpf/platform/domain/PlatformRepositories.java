package com.esmpf.platform.domain;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

interface PublicAccessTokenRepository extends JpaRepository<PublicAccessToken, UUID> {
    Optional<PublicAccessToken> findByIdAndBusinessId(UUID id, UUID businessId);
    boolean existsByBusinessIdAndTokenHash(UUID businessId, String tokenHash);
}
interface DataJobRepository extends JpaRepository<DataJob, UUID> {
    Optional<DataJob> findByIdAndBusinessId(UUID id, UUID businessId);
    Page<DataJob> findAllByBusinessId(UUID businessId, Pageable pageable);
}
interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    Optional<OutboxEvent> findByIdAndBusinessId(UUID id, UUID businessId);
    Page<OutboxEvent> findAllByBusinessId(UUID businessId, Pageable pageable);
}
interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    Page<AuditLog> findAllByBusinessId(UUID businessId, Pageable pageable);
}
interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, UUID> {
    Optional<IdempotencyRecord> findByIdAndBusinessId(UUID id, UUID businessId);
    Optional<IdempotencyRecord> findByBusinessIdAndIdempotencyKeyAndOperation(UUID businessId, String key, String operation);
}
interface IntegrationConnectionRepository extends JpaRepository<IntegrationConnection, UUID> {
    Optional<IntegrationConnection> findByIdAndBusinessId(UUID id, UUID businessId);
    Page<IntegrationConnection> findAllByBusinessId(UUID businessId, Pageable pageable);
    boolean existsByBusinessIdAndNameIgnoreCase(UUID businessId, String name);
}
interface DocumentSequenceRepository extends JpaRepository<DocumentSequence, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<DocumentSequence> findByBusinessIdAndDocumentTypeAndSequenceYear(UUID businessId, String documentType, Integer year);
}