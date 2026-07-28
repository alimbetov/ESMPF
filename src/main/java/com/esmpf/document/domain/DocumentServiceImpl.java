package com.esmpf.document.domain;

import static com.esmpf.document.DocumentDtos.*;

import com.esmpf.document.DocumentReferenceQuery;
import com.esmpf.document.DocumentService;
import com.esmpf.shared.exception.EntityNotFoundException;
import com.esmpf.shared.exception.StaleEntityException;
import com.esmpf.shared.tenant.TenantContext;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class DocumentServiceImpl implements DocumentService, DocumentReferenceQuery {
    private final TenantContext tenantContext;
    private final ReportTemplateRepository templateRepository;
    private final GeneratedDocumentRepository documentRepository;
    private final AttachmentRepository attachmentRepository;
    private final AttachmentLinkRepository linkRepository;
    private final DocumentSignatureRepository signatureRepository;
    private final DocumentMapper mapper;

    @Override @Transactional
    public ReportTemplateResponse createTemplate(ReportTemplateCommand command) {
        validateTemplate(command);
        if (templateRepository.existsByBusinessIdAndCodeIgnoreCaseAndLocaleIgnoreCaseAndTemplateVersion(
                tenant(), command.code(), command.locale(), command.templateVersion())) {
            throw new IllegalArgumentException("Template version already exists");
        }
        ReportTemplate entity = mapper.toEntity(command);
        entity.setBusinessId(tenant());
        return mapper.toResponse(templateRepository.saveAndFlush(entity));
    }

    @Override @Transactional(readOnly = true)
    public ReportTemplateResponse getTemplate(UUID templateId) {
        return mapper.toResponse(requireTemplate(templateId));
    }

    @Override @Transactional
    public ReportTemplateResponse updateDraftTemplate(UUID id, ReportTemplateCommand command) {
        ReportTemplate entity = requireTemplate(id);
        checkVersion("ReportTemplate", id, command.version(), entity.getVersion());
        requireStatus(entity.getStatus(), "DRAFT");
        validateTemplate(command);
        mapper.update(command, entity);
        return mapper.toResponse(templateRepository.saveAndFlush(entity));
    }

    @Override @Transactional
    public ReportTemplateResponse publishTemplate(UUID id, long version) {
        ReportTemplate entity = requireTemplate(id);
        checkVersion("ReportTemplate", id, version, entity.getVersion());
        requireStatus(entity.getStatus(), "DRAFT");
        entity.setStatus("PUBLISHED");
        entity.setPublishedAt(Instant.now());
        return mapper.toResponse(templateRepository.saveAndFlush(entity));
    }

    @Override @Transactional
    public ReportTemplateResponse archiveTemplate(UUID id, long version) {
        ReportTemplate entity = requireTemplate(id);
        checkVersion("ReportTemplate", id, version, entity.getVersion());
        if ("ARCHIVED".equals(entity.getStatus())) {
            throw new IllegalStateException("Template already archived");
        }
        entity.setStatus("ARCHIVED");
        return mapper.toResponse(templateRepository.saveAndFlush(entity));
    }

    @Override @Transactional(readOnly = true)
    public Page<ReportTemplateResponse> listTemplates(Pageable pageable) {
        return templateRepository.findAllByBusinessId(tenant(), pageable).map(mapper::toResponse);
    }

    @Override @Transactional
    public GeneratedDocumentResponse requestGeneration(DocumentGenerationCommand command) {
        ReportTemplate template = requireTemplate(command.reportTemplateId());
        requireStatus(template.getStatus(), "PUBLISHED");
        if (!template.getDocumentType().equals(command.documentType())) {
            throw new IllegalArgumentException("Template document type mismatch");
        }
        if (documentRepository.existsByBusinessIdAndDocumentNumberIgnoreCase(tenant(), command.documentNumber())) {
            throw new IllegalArgumentException("Document number already exists");
        }
        if (command.supersedesDocumentId() != null) {
            GeneratedDocument previous = requireDocumentEntity(command.supersedesDocumentId());
            if (!"GENERATED".equals(previous.getStatus()) && !"DELIVERED".equals(previous.getStatus())) {
                throw new IllegalStateException("Only generated documents can be superseded");
            }
        }
        GeneratedDocument entity = mapper.toEntity(command);
        entity.setBusinessId(tenant());
        return mapper.toResponse(documentRepository.saveAndFlush(entity));
    }

    @Override @Transactional
    public GeneratedDocumentResponse startGeneration(UUID id, long version) {
        GeneratedDocument entity = requireDocumentEntity(id);
        checkVersion("GeneratedDocument", id, version, entity.getVersion());
        if (!("REQUESTED".equals(entity.getStatus()) || "FAILED".equals(entity.getStatus()))) {
            throw new IllegalStateException("Document is not ready for generation");
        }
        entity.setStatus("GENERATING");
        entity.setGenerationAttempts(entity.getGenerationAttempts() + 1);
        entity.setLastError(null);
        return mapper.toResponse(documentRepository.saveAndFlush(entity));
    }

    @Override @Transactional
    public GeneratedDocumentResponse completeGeneration(
            UUID id,
            long version,
            UUID attachmentId,
            String checksum
    ) {
        GeneratedDocument entity = requireDocumentEntity(id);
        checkVersion("GeneratedDocument", id, version, entity.getVersion());
        requireStatus(entity.getStatus(), "GENERATING");
        requireAttachment(attachmentId);
        entity.setAttachmentId(attachmentId);
        entity.setChecksum(checksum);
        entity.setStatus("GENERATED");
        entity.setGeneratedAt(Instant.now());
        entity.setLastError(null);
        if (entity.getSupersedesDocumentId() != null) {
            GeneratedDocument previous = requireDocumentEntity(entity.getSupersedesDocumentId());
            previous.setStatus("SUPERSEDED");
            documentRepository.saveAndFlush(previous);
        }
        return mapper.toResponse(documentRepository.saveAndFlush(entity));
    }

    @Override @Transactional
    public GeneratedDocumentResponse failGeneration(UUID id, long version, String error) {
        GeneratedDocument entity = requireDocumentEntity(id);
        checkVersion("GeneratedDocument", id, version, entity.getVersion());
        requireStatus(entity.getStatus(), "GENERATING");
        entity.setStatus("FAILED");
        entity.setLastError(error);
        return mapper.toResponse(documentRepository.saveAndFlush(entity));
    }

    @Override @Transactional
    public GeneratedDocumentResponse markDelivered(UUID id, long version, String delivery) {
        GeneratedDocument entity = requireDocumentEntity(id);
        checkVersion("GeneratedDocument", id, version, entity.getVersion());
        requireStatus(entity.getStatus(), "GENERATED");
        entity.setStatus("DELIVERED");
        entity.setDeliveryDataJson(delivery);
        return mapper.toResponse(documentRepository.saveAndFlush(entity));
    }

    @Override @Transactional(readOnly = true)
    public GeneratedDocumentResponse getDocument(UUID id) {
        return mapper.toResponse(requireDocumentEntity(id));
    }

    @Override @Transactional(readOnly = true)
    public Page<GeneratedDocumentResponse> listDocuments(Pageable pageable) {
        return documentRepository.findAllByBusinessId(tenant(), pageable).map(mapper::toResponse);
    }

    @Override @Transactional
    public AttachmentResponse registerAttachment(AttachmentCommand command) {
        if (command.sizeBytes() == null || command.sizeBytes() < 0) {
            throw new IllegalArgumentException("sizeBytes must be non-negative");
        }
        if (attachmentRepository.existsByBusinessIdAndStorageKey(tenant(), command.storageKey())) {
            throw new IllegalArgumentException("Storage key already registered");
        }
        Attachment entity = mapper.toEntity(command);
        entity.setBusinessId(tenant());
        entity.setCreatedBy(tenantContext.requireUserId());
        return mapper.toResponse(attachmentRepository.saveAndFlush(entity));
    }

    @Override @Transactional(readOnly = true)
    public AttachmentResponse getAttachment(UUID attachmentId) {
        return mapper.toResponse(requireAttachmentEntity(attachmentId));
    }

    @Override @Transactional(readOnly = true)
    public Page<AttachmentResponse> listAttachments(Pageable pageable) {
        return attachmentRepository.findAllByBusinessId(tenant(), pageable).map(mapper::toResponse);
    }

    @Override @Transactional
    public AttachmentResponse quarantineAttachment(UUID id, long version) {
        return transitionAttachment(id, version, "QUARANTINED");
    }

    @Override @Transactional
    public AttachmentResponse archiveAttachment(UUID id, long version) {
        return transitionAttachment(id, version, "ARCHIVED");
    }

    @Override @Transactional
    public AttachmentLinkResponse linkAttachment(AttachmentLinkCommand command) {
        Attachment attachment = requireAttachmentEntity(command.attachmentId());
        if (!"ACTIVE".equals(attachment.getStatus())) {
            throw new IllegalStateException("Attachment is not active");
        }
        if (linkRepository.existsByBusinessIdAndAttachmentIdAndSubjectTypeAndSubjectIdAndPurpose(
                tenant(), command.attachmentId(), command.subjectType(), command.subjectId(), command.purpose())) {
            throw new IllegalArgumentException("Attachment link already exists");
        }
        AttachmentLink entity = mapper.toEntity(command);
        entity.setBusinessId(tenant());
        return mapper.toResponse(linkRepository.saveAndFlush(entity));
    }

    @Override @Transactional(readOnly = true)
    public AttachmentLinkResponse getAttachmentLink(UUID linkId) {
        return mapper.toResponse(requireAttachmentLink(linkId));
    }

    @Override @Transactional
    public void unlinkAttachment(UUID linkId, long version) {
        AttachmentLink entity = requireAttachmentLink(linkId);
        checkVersion("AttachmentLink", linkId, version, entity.getVersion());
        linkRepository.delete(entity);
        linkRepository.flush();
    }

    @Override @Transactional(readOnly = true)
    public Page<AttachmentLinkResponse> listAttachmentLinks(UUID attachmentId, Pageable pageable) {
        requireAttachment(attachmentId);
        return linkRepository.findAllByBusinessIdAndAttachmentId(tenant(), attachmentId, pageable)
                .map(mapper::toResponse);
    }

    @Override @Transactional
    public DocumentSignatureResponse signDocument(DocumentSignatureCommand command) {
        GeneratedDocument document = requireDocumentEntity(command.generatedDocumentId());
        if (!("GENERATED".equals(document.getStatus()) || "DELIVERED".equals(document.getStatus()))) {
            throw new IllegalStateException("Document is not signable");
        }
        if (command.signatureAttachmentId() != null) {
            requireAttachment(command.signatureAttachmentId());
        }
        if (signatureRepository.existsByBusinessIdAndGeneratedDocumentIdAndSignerTypeAndSignerName(
                tenant(), command.generatedDocumentId(), command.signerType(), command.signerName())) {
            throw new IllegalArgumentException("Signer already signed document");
        }
        DocumentSignature entity = mapper.toEntity(command);
        entity.setBusinessId(tenant());
        entity.setSignedAt(Instant.now());
        return mapper.toResponse(signatureRepository.saveAndFlush(entity));
    }

    @Override @Transactional(readOnly = true)
    public DocumentSignatureResponse getSignature(UUID signatureId) {
        return mapper.toResponse(requireSignature(signatureId));
    }

    @Override @Transactional(readOnly = true)
    public Page<DocumentSignatureResponse> listSignatures(UUID documentId, Pageable pageable) {
        requireDocumentEntity(documentId);
        return signatureRepository.findAllByBusinessIdAndGeneratedDocumentId(tenant(), documentId, pageable)
                .map(mapper::toResponse);
    }

    @Override @Transactional(readOnly = true)
    public DocumentReference requireDocument(UUID id) {
        GeneratedDocument entity = requireDocumentEntity(id);
        return new DocumentReference(
                entity.getId(),
                entity.getStatus(),
                entity.getDocumentType(),
                entity.getDocumentNumber(),
                entity.getAttachmentId()
        );
    }

    @Override @Transactional(readOnly = true)
    public void requireAttachment(UUID id) {
        requireAttachmentEntity(id);
    }

    private AttachmentResponse transitionAttachment(UUID id, long version, String status) {
        Attachment entity = requireAttachmentEntity(id);
        checkVersion("Attachment", id, version, entity.getVersion());
        if (status.equals(entity.getStatus())) {
            throw new IllegalStateException("Attachment already " + status);
        }
        entity.setStatus(status);
        return mapper.toResponse(attachmentRepository.saveAndFlush(entity));
    }

    private ReportTemplate requireTemplate(UUID id) {
        return templateRepository.findByIdAndBusinessId(id, tenant())
                .orElseThrow(() -> new EntityNotFoundException("ReportTemplate", id));
    }

    private GeneratedDocument requireDocumentEntity(UUID id) {
        return documentRepository.findByIdAndBusinessId(id, tenant())
                .orElseThrow(() -> new EntityNotFoundException("GeneratedDocument", id));
    }

    private Attachment requireAttachmentEntity(UUID id) {
        return attachmentRepository.findByIdAndBusinessId(id, tenant())
                .orElseThrow(() -> new EntityNotFoundException("Attachment", id));
    }

    private AttachmentLink requireAttachmentLink(UUID id) {
        return linkRepository.findByIdAndBusinessId(id, tenant())
                .orElseThrow(() -> new EntityNotFoundException("AttachmentLink", id));
    }

    private DocumentSignature requireSignature(UUID id) {
        return signatureRepository.findByIdAndBusinessId(id, tenant())
                .orElseThrow(() -> new EntityNotFoundException("DocumentSignature", id));
    }

    private UUID tenant() {
        return tenantContext.requireBusinessId();
    }

    private static void validateTemplate(ReportTemplateCommand command) {
        if (command.templateVersion() == null || command.templateVersion() <= 0) {
            throw new IllegalArgumentException("templateVersion must be positive");
        }
        if (command.templateContent() == null || command.templateContent().isBlank()) {
            throw new IllegalArgumentException("templateContent is required");
        }
    }

    private static void requireStatus(String actual, String expected) {
        if (!expected.equals(actual)) {
            throw new IllegalStateException("Expected status " + expected + " but was " + actual);
        }
    }

    private static void checkVersion(String type, UUID id, long expected, long actual) {
        if (expected != actual) {
            throw new StaleEntityException(type, id, expected, actual);
        }
    }
}
