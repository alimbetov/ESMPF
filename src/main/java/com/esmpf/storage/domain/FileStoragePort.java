package com.esmpf.storage.domain;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

interface FileStoragePort {
    StorageWriteResult store(StorageWriteRequest request) throws IOException;
    StoredContent open(String storageKey) throws IOException;
    void delete(String storageKey) throws IOException;
    boolean exists(String storageKey);

    record StorageWriteRequest(UUID businessId, UUID fileId, InputStream content, long maximumBytes) {}
    record StorageWriteResult(String storageKey, long size, String checksumSha256) {}
    record StoredContent(InputStream inputStream, long contentLength) implements AutoCloseable {
        @Override public void close() throws IOException { inputStream.close(); }
    }
}
