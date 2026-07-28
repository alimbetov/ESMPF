package com.esmpf.document;

import static com.esmpf.document.DocumentDtos.*;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DocumentService {
    ReportTemplateResponse createTemplate(ReportTemplateCommand command);
    ReportTemplateResponse getTemplate(UUID templateId);
    ReportTemplateResponse updateDraftTemplate(UUID templateId, ReportTemplateCommand command);
    ReportTemplateResponse publishTemplate(UUID templateId, long version);
    ReportTemplateResponse archiveTemplate(UUID templateId, long version);
    Page<ReportTemplateResponse> listTemplates(Pageable pageable);

    GeneratedDocumentResponse requestGeneration(DocumentGenerationCommand command);
    GeneratedDocumentResponse startGeneration(UUID documentId, long version);
    GeneratedDocumentResponse completeGeneration(UUID documentId, long version, UUID attachmentId, String checksum);
    GeneratedDocumentResponse failGeneration(UUID documentId, long version, String error);
    GeneratedDocumentResponse markDelivered(UUID documentId, long version, String deliveryDataJson);
    GeneratedDocumentResponse getDocument(UUID documentId);
    Page<GeneratedDocumentResponse> listDocuments(Pageable pageable);

    AttachmentResponse registerAttachment(AttachmentCommand command);
    AttachmentResponse getAttachment(UUID attachmentId);
    Page<AttachmentResponse> listAttachments(Pageable pageable);
    AttachmentResponse quarantineAttachment(UUID attachmentId, long version);
    AttachmentResponse archiveAttachment(UUID attachmentId, long version);
    AttachmentLinkResponse linkAttachment(AttachmentLinkCommand command);
    AttachmentLinkResponse getAttachmentLink(UUID linkId);
    void unlinkAttachment(UUID linkId, long version);
    Page<AttachmentLinkResponse> listAttachmentLinks(UUID attachmentId, Pageable pageable);

    DocumentSignatureResponse signDocument(DocumentSignatureCommand command);
    DocumentSignatureResponse getSignature(UUID signatureId);
    Page<DocumentSignatureResponse> listSignatures(UUID documentId, Pageable pageable);
}
