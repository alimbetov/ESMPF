package com.esmpf.storage.domain;

import static com.esmpf.storage.StorageDtos.FileResponse;

import com.esmpf.shared.exception.EntityNotFoundException;
import com.esmpf.shared.exception.StaleEntityException;
import com.esmpf.shared.tenant.TenantContext;
import com.esmpf.storage.StorageService;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
class StorageServiceImpl implements StorageService {
    private static final Set<String> ALLOWED = Set.of("image/jpeg", "image/png", "application/pdf", "text/plain");

    private final TenantContext tenantContext;
    private final StoredFileRepository repository;
    private final FileStoragePort storage;

    @Value("${esmpf.storage.file.max-size:104857600}")
    private long maximumBytes;

    @Override
    public FileResponse upload(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("file is required");
        String original = requireFileName(file.getOriginalFilename());
        String normalized = normalizeFileName(original);
        String detected;
        StoredFile entity = createCreating(original, normalized, file.getContentType());
        try (BufferedInputStream input = new BufferedInputStream(file.getInputStream())) {
            input.mark(32);
            detected = detectMime(input, normalized);
            input.reset();
            if (!ALLOWED.contains(detected)) throw new UnsupportedFileTypeException(detected);
            FileStoragePort.StorageWriteResult written = storage.store(new FileStoragePort.StorageWriteRequest(
                    entity.getBusinessId(), entity.getId(), input, maximumBytes));
            return complete(entity.getId(), detected, written);
        } catch (RuntimeException | IOException failure) {
            fail(entity.getId());
            throw failure;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    StoredFile createCreating(String original, String normalized, String declaredMime) {
        StoredFile entity = new StoredFile();
        entity.setBusinessId(tenant());
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
    StoredFile complete(UUID id, String detectedMime, FileStoragePort.StorageWriteResult written) {
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
    void fail(UUID id) {
        repository.findByIdAndBusinessId(id, tenant()).ifPresent(entity -> {
            entity.setStatus(StoredFileStatus.FAILED);
            repository.saveAndFlush(entity);
        });
    }

    @Override @Transactional(readOnly = true)
    public FileResponse metadata(UUID fileId) { return response(require(fileId)); }

    @Override @Transactional(readOnly = true)
    public Page<FileResponse> list(Pageable pageable) { return repository.findAllByBusinessId(tenant(), pageable).map(this::response); }

    @Override @Transactional(readOnly = true)
    public FileDownload download(UUID fileId) {
        StoredFile entity = require(fileId);
        if (entity.getStatus() != StoredFileStatus.AVAILABLE) throw new IllegalStateException("File is not available");
        try {
            FileStoragePort.StoredContent content = storage.open(entity.getStorageKey());
            return new FileDownload(entity.getNormalizedFileName(), entity.getDetectedMimeType(), content.contentLength(), content.inputStream());
        } catch (IOException failure) {
            throw new StorageUnavailableException(failure);
        }
    }

    @Override @Transactional
    public FileResponse delete(UUID fileId, long version) {
        StoredFile entity = require(fileId);
        checkVersion(fileId, version, entity.getVersion());
        if (entity.getStatus() != StoredFileStatus.AVAILABLE) throw new IllegalStateException("Only available files can be deleted");
        entity.setStatus(StoredFileStatus.DELETED);
        entity.setDeletedAt(Instant.now());
        entity.setDeletedBy(tenantContext.requireUserId());
        return response(repository.saveAndFlush(entity));
    }

    @Override @Transactional
    public FileResponse restore(UUID fileId, long version) {
        StoredFile entity = require(fileId);
        checkVersion(fileId, version, entity.getVersion());
        if (entity.getStatus() != StoredFileStatus.DELETED || entity.getPhysicalDeletedAt() != null) {
            throw new IllegalStateException("File cannot be restored");
        }
        if (!storage.exists(entity.getStorageKey())) throw new StorageUnavailableException(null);
        entity.setStatus(StoredFileStatus.AVAILABLE);
        entity.setDeletedAt(null);
        entity.setDeletedBy(null);
        return response(repository.saveAndFlush(entity));
    }

    private StoredFile require(UUID id) {
        return repository.findByIdAndBusinessId(id, tenant())
                .orElseThrow(() -> new EntityNotFoundException("StoredFile", id));
    }

    private FileResponse response(StoredFile file) {
        return new FileResponse(file.getId(), file.getVersion(), file.getOriginalFileName(), file.getDetectedMimeType(),
                file.getFileSize(), file.getChecksumSha256(), file.getStatus().name(), file.getCreatedAt(), file.getCreatedBy());
    }

    private UUID tenant() { return tenantContext.requireBusinessId(); }

    private static void checkVersion(UUID id, long expected, long actual) {
        if (expected != actual) throw new StaleEntityException("StoredFile", id, expected, actual);
    }

    private static String requireFileName(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Original file name is required");
        return value;
    }

    private static String normalizeFileName(String value) {
        String normalized = value.replace('\\', '/');
        normalized = normalized.substring(normalized.lastIndexOf('/') + 1)
                .replaceAll("[\\r\\n\\u0000-\\u001F\\u007F]", "_")
                .trim();
        if (normalized.isBlank() || normalized.length() > 255) throw new IllegalArgumentException("Invalid file name");
        return normalized;
    }

    private static String detectMime(InputStream input, String fileName) throws IOException {
        byte[] header = input.readNBytes(16);
        if (header.length >= 4 && header[0] == 0x25 && header[1] == 0x50 && header[2] == 0x44 && header[3] == 0x46) return "application/pdf";
        if (header.length >= 8 && header[0] == (byte) 0x89 && header[1] == 0x50 && header[2] == 0x4e && header[3] == 0x47) return "image/png";
        if (header.length >= 3 && header[0] == (byte) 0xff && header[1] == (byte) 0xd8 && header[2] == (byte) 0xff) return "image/jpeg";
        String lower = fileName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".txt")) return "text/plain";
        return "application/octet-stream";
    }
}

final class UnsupportedFileTypeException extends IllegalArgumentException {
    UnsupportedFileTypeException(String mimeType) { super("File type is not allowed: " + mimeType); }
}

final class StorageUnavailableException extends IllegalStateException {
    StorageUnavailableException(Throwable cause) { super("Storage is unavailable", cause); }
}
