package com.esmpf.storage;

import static com.esmpf.storage.StorageDtos.FileResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    FileResponse upload(MultipartFile file) throws IOException;
    FileResponse metadata(UUID fileId);
    Page<FileResponse> list(Pageable pageable);
    FileDownload download(UUID fileId);
    FileResponse delete(UUID fileId, long version);
    FileResponse restore(UUID fileId, long version);

    record FileDownload(String fileName, String mimeType, long contentLength, InputStream content) implements AutoCloseable {
        @Override public void close() throws IOException { content.close(); }
    }
}
