package com.esmpf.storage;

import java.time.Instant;
import java.util.UUID;

public final class StorageDtos {
    private StorageDtos() {}

    public record FileResponse(
            UUID id,
            long version,
            String originalFileName,
            String mimeType,
            long fileSize,
            String checksumSha256,
            String status,
            Instant createdAt,
            UUID createdBy
    ) {}
}
