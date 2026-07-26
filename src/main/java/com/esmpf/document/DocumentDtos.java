package com.esmpf.document;

import java.time.Instant;
import java.util.UUID;

public final class DocumentDtos {
    private DocumentDtos() {}

    public record ReportTemplateCommand(long version, String code, String documentType, String locale,
                                        Integer templateVersion, String templateContent,
                                        String stylesheetContent, String configurationJson) {}
    public record ReportTemplateResponse(UUID id, long version, String code, String documentType,
                                         String locale, Integer templateVersion, String templateContent,
                                         String stylesheetContent, String configurationJson,
                                         String status, Instant publishedAt,
                                         Instant createdAt, Instant updatedAt) {}

    public record DocumentGenerationCommand(String documentType, String documentNumber,
                                            String sourceType, UUID sourceId, UUID reportTemplateId,
                                            String snapshotJson, UUID supersedesDocumentId) {}
    public record GeneratedDocumentResponse(UUID id, long version, String documentType,
                                            String documentNumber, String sourceType, UUID sourceId,
                                            UUID reportTemplateId, String snapshotJson, String status,
                                            UUID attachmentId, String checksum, Integer generationAttempts,
                                            String lastError, Instant generatedAt,
                                            UUID supersedesDocumentId, String deliveryDataJson,
                                            Instant createdAt, Instant updatedAt) {}

    public record AttachmentCommand(String storageKey, String fileName, String contentType,
                                    Long sizeBytes, String checksum, String metadataJson) {}
    public record AttachmentResponse(UUID id, long version, String storageKey, String fileName,
                                     String contentType, Long sizeBytes, String checksum,
                                     String metadataJson, UUID createdBy, String status,
                                     Instant createdAt, Instant updatedAt) {}

    public record AttachmentLinkCommand(UUID attachmentId, String subjectType, UUID subjectId,
                                        String purpose) {}
    public record AttachmentLinkResponse(UUID id, long version, UUID attachmentId,
                                         String subjectType, UUID subjectId, String purpose,
                                         Instant createdAt, Instant updatedAt) {}

    public record DocumentSignatureCommand(UUID generatedDocumentId, String signerType,
                                           String signerName, UUID signerUserId, String method,
                                           UUID signatureAttachmentId, String ipAddress,
                                           String userAgent, String verificationDataJson) {}
    public record DocumentSignatureResponse(UUID id, long version, UUID generatedDocumentId,
                                            String signerType, String signerName, UUID signerUserId,
                                            String method, UUID signatureAttachmentId, Instant signedAt,
                                            String ipAddress, String userAgent,
                                            String verificationDataJson,
                                            Instant createdAt, Instant updatedAt) {}

    public record DocumentReference(UUID id, String status, String documentType,
                                    String documentNumber, UUID attachmentId) {}
}