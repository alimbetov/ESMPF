package com.esmpf.document.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface ReportTemplateRepository extends JpaRepository<ReportTemplate, UUID> {
    Optional<ReportTemplate> findByIdAndBusinessId(UUID id, UUID businessId);
    Page<ReportTemplate> findAllByBusinessId(UUID businessId, Pageable pageable);
    boolean existsByBusinessIdAndCodeIgnoreCaseAndLocaleIgnoreCaseAndTemplateVersion(
            UUID businessId,
            String code,
            String locale,
            Integer version
    );
}

interface GeneratedDocumentRepository extends JpaRepository<GeneratedDocument, UUID> {
    Optional<GeneratedDocument> findByIdAndBusinessId(UUID id, UUID businessId);
    Page<GeneratedDocument> findAllByBusinessId(UUID businessId, Pageable pageable);
    boolean existsByBusinessIdAndDocumentNumberIgnoreCase(UUID businessId, String number);
}

interface AttachmentRepository extends JpaRepository<Attachment, UUID> {
    Optional<Attachment> findByIdAndBusinessId(UUID id, UUID businessId);
    Page<Attachment> findAllByBusinessId(UUID businessId, Pageable pageable);
    boolean existsByBusinessIdAndStorageKey(UUID businessId, String storageKey);
}

interface AttachmentLinkRepository extends JpaRepository<AttachmentLink, UUID> {
    Optional<AttachmentLink> findByIdAndBusinessId(UUID id, UUID businessId);
    Page<AttachmentLink> findAllByBusinessIdAndAttachmentId(
            UUID businessId,
            UUID attachmentId,
            Pageable pageable
    );
    boolean existsByBusinessIdAndAttachmentIdAndSubjectTypeAndSubjectIdAndPurpose(
            UUID businessId,
            UUID attachmentId,
            String subjectType,
            UUID subjectId,
            String purpose
    );
}

interface DocumentSignatureRepository extends JpaRepository<DocumentSignature, UUID> {
    Optional<DocumentSignature> findByIdAndBusinessId(UUID id, UUID businessId);
    Page<DocumentSignature> findAllByBusinessIdAndGeneratedDocumentId(
            UUID businessId,
            UUID documentId,
            Pageable pageable
    );
    boolean existsByBusinessIdAndGeneratedDocumentIdAndSignerTypeAndSignerName(
            UUID businessId,
            UUID documentId,
            String signerType,
            String signerName
    );
}
