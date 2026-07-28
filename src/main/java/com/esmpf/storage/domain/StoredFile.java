package com.esmpf.storage.domain;

import com.esmpf.shared.persistence.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "stored_file", indexes = {
        @Index(name = "idx_stored_file_business_status_created", columnList = "business_id,status,created_at"),
        @Index(name = "idx_stored_file_business_checksum", columnList = "business_id,checksum_sha256")
})
class StoredFile extends TenantEntity {
    @Column(name = "original_file_name", nullable = false, length = 255) private String originalFileName;
    @Column(name = "normalized_file_name", nullable = false, length = 255) private String normalizedFileName;
    @Column(name = "storage_key", unique = true, length = 512) private String storageKey;
    @Column(name = "declared_mime_type", length = 127) private String declaredMimeType;
    @Column(name = "detected_mime_type", nullable = false, length = 127) private String detectedMimeType;
    @Column(name = "file_size", nullable = false) private long fileSize;
    @Column(name = "checksum_sha256", length = 64) private String checksumSha256;
    @Enumerated(EnumType.STRING) @Column(name = "storage_provider", nullable = false, length = 32) private StorageProvider storageProvider;
    @Enumerated(EnumType.STRING) @Column(name = "status", nullable = false, length = 32) private StoredFileStatus status;
    @Column(name = "created_by", nullable = false) private UUID createdBy;
    @Column(name = "available_at") private Instant availableAt;
    @Column(name = "deleted_at") private Instant deletedAt;
    @Column(name = "deleted_by") private UUID deletedBy;
    @Column(name = "physical_deleted_at") private Instant physicalDeletedAt;
}

enum StorageProvider { LOCAL, S3 }
enum StoredFileStatus { CREATING, AVAILABLE, FAILED, DELETED, PURGED, QUARANTINED, SCANNING, INFECTED, REJECTED, SCAN_FAILED }
