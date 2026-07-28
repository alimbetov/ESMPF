package com.esmpf.storage.domain;

import com.esmpf.shared.exception.EntityNotFoundException;
import com.esmpf.shared.tenant.TenantContext;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class StoredFileLifecycleService {
    private final TenantContext tenantContext;
    private final StoredFileRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    StoredFile createCreating(String original, String normalized, String declaredMime) {
        StoredFile entity = new StoredFile();
        entity.setBusinessId(tenantContext.requireBusinessId());
        entity.setOriginalFileName(original);
        entity.setNormalizedFileName(normalized);
        entity.setDeclaredMimeType(declaredMime);
        entity.setDetectedMimeType("application/octet-stream");
        entity.setStorageProvider(StorageProvider.LOCAL);
        entity.setStatus(StoredFileStatus.CREATING);
        entity.setCreatedBy(tenantContext.requireUserId());
        entity.setFileSize(0);
        return repository.saveAndFlush(entity);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    StoredFile markAvailable(UUID id, String detectedMime, FileStoragePort.StorageWriteResult written) {
        StoredFile entity = require(id);
        entity.setStorageKey(written.storageKey());
        entity.setFileSize(written.size());
        entity.setChecksumSha256(written.checksumSha256());
        entity.setDetectedMimeType(detectedMime);
        entity.setAvailableAt(Instant.now());
        entity.setStatus(StoredFileStatus.AVAILABLE);
        return repository.saveAndFlush(entity);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void markFailed(UUID id) {
        repository.findByIdAndBusinessId(id, tenantContext.requireBusinessId()).ifPresent(entity -> {
            entity.setStatus(StoredFileStatus.FAILED);
            repository.saveAndFlush(entity);
        });
    }

    private StoredFile require(UUID id) {
        return repository.findByIdAndBusinessId(id, tenantContext.requireBusinessId())
                .orElseThrow(() -> new EntityNotFoundException("StoredFile", id));
    }
}
