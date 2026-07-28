package com.esmpf.storage;

import static com.esmpf.storage.StorageDtos.FileResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class StorageRestController {
    private final StorageService service;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public FileResponse upload(@RequestPart("file") MultipartFile file) throws IOException {
        return service.upload(file);
    }

    @GetMapping
    public Page<FileResponse> list(Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/{fileId}/metadata")
    public FileResponse metadata(@PathVariable UUID fileId) {
        return service.metadata(fileId);
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<StreamingResponseBody> download(@PathVariable UUID fileId) {
        StorageService.FileDownload download = service.download(fileId);
        StreamingResponseBody body = output -> {
            try (download) {
                download.content().transferTo(output);
            }
        };
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition(download.fileName()))
                .header("X-Content-Type-Options", "nosniff")
                .header(HttpHeaders.CACHE_CONTROL, "private, no-store")
                .contentType(MediaType.parseMediaType(download.mimeType()))
                .contentLength(download.contentLength())
                .body(body);
    }

    @DeleteMapping("/{fileId}")
    public FileResponse delete(@PathVariable UUID fileId, @RequestParam long version) {
        return service.delete(fileId, version);
    }

    @PostMapping("/{fileId}/actions/restore")
    public FileResponse restore(@PathVariable UUID fileId, @RequestParam long version) {
        return service.restore(fileId, version);
    }

    private static String contentDisposition(String fileName) {
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        return "attachment; filename*=UTF-8''" + encoded;
    }
}
