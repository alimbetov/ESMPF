package com.esmpf.document;

import static com.esmpf.document.DocumentDtos.*;
import static com.esmpf.web.ApiActionRequests.GeneratedDocumentCompleteRequest;
import static com.esmpf.web.ApiActionRequests.JsonRequest;
import static com.esmpf.web.ApiActionRequests.VersionRequest;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DocumentRestController {
    private final DocumentService service;

    @PostMapping("/report-templates")
    @ResponseStatus(HttpStatus.CREATED)
    public ReportTemplateResponse createTemplate(@Valid @RequestBody ReportTemplateCommand command) {
        return service.createTemplate(command);
    }

    @GetMapping("/report-templates/{templateId}")
    public ReportTemplateResponse getTemplate(@PathVariable UUID templateId) {
        return service.getTemplate(templateId);
    }

    @PutMapping("/report-templates/{templateId}")
    public ReportTemplateResponse updateDraftTemplate(
            @PathVariable UUID templateId,
            @Valid @RequestBody ReportTemplateCommand command
    ) {
        return service.updateDraftTemplate(templateId, command);
    }

    @PostMapping("/report-templates/{templateId}/actions/publish")
    public ReportTemplateResponse publishTemplate(
            @PathVariable UUID templateId,
            @Valid @RequestBody VersionRequest request
    ) {
        return service.publishTemplate(templateId, request.version());
    }

    @PostMapping("/report-templates/{templateId}/actions/archive")
    public ReportTemplateResponse archiveTemplate(
            @PathVariable UUID templateId,
            @Valid @RequestBody VersionRequest request
    ) {
        return service.archiveTemplate(templateId, request.version());
    }

    @GetMapping("/report-templates")
    public Page<ReportTemplateResponse> listTemplates(Pageable pageable) {
        return service.listTemplates(pageable);
    }

    @PostMapping("/generated-documents")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public GeneratedDocumentResponse requestGeneration(@Valid @RequestBody DocumentGenerationCommand command) {
        return service.requestGeneration(command);
    }

    @PostMapping("/generated-documents/{documentId}/actions/start")
    public GeneratedDocumentResponse startGeneration(
            @PathVariable UUID documentId,
            @Valid @RequestBody VersionRequest request
    ) {
        return service.startGeneration(documentId, request.version());
    }

    @PostMapping("/generated-documents/{documentId}/actions/complete")
    public GeneratedDocumentResponse completeGeneration(
            @PathVariable UUID documentId,
            @Valid @RequestBody GeneratedDocumentCompleteRequest request
    ) {
        return service.completeGeneration(
                documentId,
                request.version(),
                request.attachmentId(),
                request.checksum()
        );
    }

    @PostMapping("/generated-documents/{documentId}/actions/fail")
    public GeneratedDocumentResponse failGeneration(
            @PathVariable UUID documentId,
            @Valid @RequestBody JsonRequest request
    ) {
        return service.failGeneration(documentId, request.version(), request.dataJson());
    }

    @PostMapping("/generated-documents/{documentId}/actions/mark-delivered")
    public GeneratedDocumentResponse markDelivered(
            @PathVariable UUID documentId,
            @Valid @RequestBody JsonRequest request
    ) {
        return service.markDelivered(documentId, request.version(), request.dataJson());
    }

    @GetMapping("/generated-documents/{documentId}")
    public GeneratedDocumentResponse getDocument(@PathVariable UUID documentId) {
        return service.getDocument(documentId);
    }

    @GetMapping("/generated-documents")
    public Page<GeneratedDocumentResponse> listDocuments(Pageable pageable) {
        return service.listDocuments(pageable);
    }

    @PostMapping("/attachments")
    @ResponseStatus(HttpStatus.CREATED)
    public AttachmentResponse registerAttachment(@Valid @RequestBody AttachmentCommand command) {
        return service.registerAttachment(command);
    }

    @GetMapping("/attachments/{attachmentId}")
    public AttachmentResponse getAttachment(@PathVariable UUID attachmentId) {
        return service.getAttachment(attachmentId);
    }

    @GetMapping("/attachments")
    public Page<AttachmentResponse> listAttachments(Pageable pageable) {
        return service.listAttachments(pageable);
    }

    @PostMapping("/attachments/{attachmentId}/actions/quarantine")
    public AttachmentResponse quarantineAttachment(
            @PathVariable UUID attachmentId,
            @Valid @RequestBody VersionRequest request
    ) {
        return service.quarantineAttachment(attachmentId, request.version());
    }

    @PostMapping("/attachments/{attachmentId}/actions/archive")
    public AttachmentResponse archiveAttachment(
            @PathVariable UUID attachmentId,
            @Valid @RequestBody VersionRequest request
    ) {
        return service.archiveAttachment(attachmentId, request.version());
    }

    @PostMapping("/attachment-links")
    @ResponseStatus(HttpStatus.CREATED)
    public AttachmentLinkResponse linkAttachment(@Valid @RequestBody AttachmentLinkCommand command) {
        return service.linkAttachment(command);
    }

    @GetMapping("/attachment-links/{linkId}")
    public AttachmentLinkResponse getAttachmentLink(@PathVariable UUID linkId) {
        return service.getAttachmentLink(linkId);
    }

    @DeleteMapping("/attachment-links/{linkId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlinkAttachment(
            @PathVariable UUID linkId,
            @RequestParam long version
    ) {
        service.unlinkAttachment(linkId, version);
    }

    @GetMapping("/attachments/{attachmentId}/links")
    public Page<AttachmentLinkResponse> listAttachmentLinks(
            @PathVariable UUID attachmentId,
            Pageable pageable
    ) {
        return service.listAttachmentLinks(attachmentId, pageable);
    }

    @PostMapping("/document-signatures")
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentSignatureResponse signDocument(@Valid @RequestBody DocumentSignatureCommand command) {
        return service.signDocument(command);
    }

    @GetMapping("/document-signatures/{signatureId}")
    public DocumentSignatureResponse getSignature(@PathVariable UUID signatureId) {
        return service.getSignature(signatureId);
    }

    @GetMapping("/generated-documents/{documentId}/signatures")
    public Page<DocumentSignatureResponse> listSignatures(
            @PathVariable UUID documentId,
            Pageable pageable
    ) {
        return service.listSignatures(documentId, pageable);
    }
}
