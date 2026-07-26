package com.esmpf.document.domain;

import com.esmpf.shared.persistence.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "report_template", indexes = @Index(name = "idx_report_template_business", columnList = "business_id"))
class ReportTemplate extends TenantEntity {
    @Column(name = "code", nullable = false, length = 100) private String code;
    @Column(name = "document_type", nullable = false, length = 80) private String documentType;
    @Column(name = "locale", nullable = false, length = 20) private String locale;
    @Column(name = "template_version", nullable = false) private Integer templateVersion;
    @Lob @Column(name = "template_content", nullable = false) private String templateContent;
    @Lob @Column(name = "stylesheet_content") private String stylesheetContent;
    @Lob @Column(name = "configuration_json") private String configurationJson;
    @Column(name = "status", nullable = false, length = 40) private String status;
    @Column(name = "published_at") private Instant publishedAt;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "generated_document", indexes = @Index(name = "idx_generated_document_business", columnList = "business_id"))
class GeneratedDocument extends TenantEntity {
    @Column(name = "document_type", nullable = false, length = 80) private String documentType;
    @Column(name = "document_number", nullable = false, length = 120) private String documentNumber;
    @Column(name = "source_type", nullable = false, length = 80) private String sourceType;
    @Column(name = "source_id", nullable = false) private UUID sourceId;
    @Column(name = "report_template_id", nullable = false) private UUID reportTemplateId;
    @Lob @Column(name = "snapshot_json", nullable = false) private String snapshotJson;
    @Column(name = "status", nullable = false, length = 40) private String status;
    @Column(name = "attachment_id") private UUID attachmentId;
    @Column(name = "checksum", length = 128) private String checksum;
    @Column(name = "generation_attempts", nullable = false) private Integer generationAttempts;
    @Lob @Column(name = "last_error") private String lastError;
    @Column(name = "generated_at") private Instant generatedAt;
    @Column(name = "supersedes_document_id") private UUID supersedesDocumentId;
    @Lob @Column(name = "delivery_data_json") private String deliveryDataJson;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "document_signature", indexes = @Index(name = "idx_document_signature_business", columnList = "business_id"))
class DocumentSignature extends TenantEntity {
    @Column(name = "generated_document_id", nullable = false) private UUID generatedDocumentId;
    @Column(name = "signer_type", nullable = false, length = 60) private String signerType;
    @Column(name = "signer_name", nullable = false, length = 200) private String signerName;
    @Column(name = "signer_user_id") private UUID signerUserId;
    @Column(name = "method", nullable = false, length = 60) private String method;
    @Column(name = "signature_attachment_id") private UUID signatureAttachmentId;
    @Column(name = "signed_at", nullable = false) private Instant signedAt;
    @Column(name = "ip_address", length = 64) private String ipAddress;
    @Column(name = "user_agent", length = 1000) private String userAgent;
    @Lob @Column(name = "verification_data_json") private String verificationDataJson;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "attachment", indexes = @Index(name = "idx_attachment_business", columnList = "business_id"))
class Attachment extends TenantEntity {
    @Column(name = "storage_key", nullable = false, length = 500) private String storageKey;
    @Column(name = "file_name", nullable = false, length = 500) private String fileName;
    @Column(name = "content_type", nullable = false, length = 200) private String contentType;
    @Column(name = "size_bytes", nullable = false) private Long sizeBytes;
    @Column(name = "checksum", nullable = false, length = 128) private String checksum;
    @Lob @Column(name = "metadata_json") private String metadataJson;
    @Column(name = "created_by") private UUID createdBy;
    @Column(name = "status", nullable = false, length = 40) private String status;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "attachment_link", indexes = @Index(name = "idx_attachment_link_business", columnList = "business_id"))
class AttachmentLink extends TenantEntity {
    @Column(name = "attachment_id", nullable = false) private UUID attachmentId;
    @Column(name = "subject_type", nullable = false, length = 80) private String subjectType;
    @Column(name = "subject_id", nullable = false) private UUID subjectId;
    @Column(name = "purpose", nullable = false, length = 80) private String purpose;
}
