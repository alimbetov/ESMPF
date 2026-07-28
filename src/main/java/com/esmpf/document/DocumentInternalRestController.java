package com.esmpf.document;

import static com.esmpf.document.DocumentDtos.GeneratedDocumentResponse;
import static com.esmpf.web.ApiActionRequests.*;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/v1/generated-documents")
@RequiredArgsConstructor
public class DocumentInternalRestController {
    private final DocumentService service;

    @PostMapping("/{documentId}/actions/start")
    public GeneratedDocumentResponse startGeneration(@PathVariable UUID documentId,
                                                      @Valid @RequestBody VersionRequest request) {
        return service.startGeneration(documentId, request.version());
    }

    @PostMapping("/{documentId}/actions/complete")
    public GeneratedDocumentResponse completeGeneration(@PathVariable UUID documentId,
                                                         @Valid @RequestBody GeneratedDocumentCompleteRequest request) {
        return service.completeGeneration(documentId, request.version(), request.attachmentId(), request.checksum());
    }

    @PostMapping("/{documentId}/actions/fail")
    public GeneratedDocumentResponse failGeneration(@PathVariable UUID documentId,
                                                     @Valid @RequestBody JsonRequest request) {
        return service.failGeneration(documentId, request.version(), request.dataJson());
    }
}
