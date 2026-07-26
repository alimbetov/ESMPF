package com.esmpf.document.domain;

import com.esmpf.shared.persistence.BaseEntity;
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
class ReportTemplate extends BaseEntity {
    @Column(name = "code") private String code;
    @Column(name = "document_type") private String documentType;
    @Column(name = "locale") private String locale;
    @Column(name = "template_version") private Integer templateVersion;
    @Lob @Column(name = "template_content") private String templateContent;
    @Lob @Column(name = "stylesheet_content") private String stylesheetContent;
    @Lob @Column(name = "configuration_json") private String configurationJson;
    @Column(name = "status") private String status;
    @Column(name = "published_at") private Instant publishedAt;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "generated_document", indexes = @Index(name = "idx_generated_document_business", columnList = "business_id"))
class GeneratedDocument extends BaseEntity {
    @Column(name = "document_type") private String documentType;
    @Column(name = "document_number") private String documentNumber;
    @Column(name = "source_type") private String sourceType;
    @Column(name = "source_id") private UUID sourceId;
    @Column(name = "report_template_id") private UUID reportTemplateId;
    @Lob @Column(name = "snapshot_json") private String snapshotJson;
    @Column(name = "status") private String status;
    @Column(name = "attachment_id") private UUID attachmentId;
    @Column(name = "checksum") private String checksum;
    @Column(name = "generation_attempts") private Integer generationAttempts;
    @Lob @Column(name = "last_error") private String lastError;
    @Column(name = "generated_at") private Instant generatedAt;
    @Column(name = "supersedes_document_id") private UUID supersedesDocumentId;
    @Lob @Column(name = "delivery_data_json") private String deliveryDataJson;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "document_signature", indexes = @Index(name = "idx_document_signature_business", columnList = "business_id"))
class DocumentSignature extends BaseEntity {
    @Column(name = "generated_document_id") private UUID generatedDocumentId;
    @Column(name = "signer_type") private String signerType;
    @Column(name = "signer_name") private String signerName;
    @Column(name = "signer_user_id") private UUID signerUserId;
    @Column(name = "method") private String method;
    @Column(name = "signature_attachment_id") private UUID signatureAttachmentId;
    @Column(name = "signed_at") private Instant signedAt;
    @Column(name = "ip_address") private String ipAddress;
    @Column(name = "user_agent") private String userAgent;
    @Lob @Column(name = "verification_data_json") private String verificationDataJson;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "attachment", indexes = @Index(name = "idx_attachment_business", columnList = "business_id"))
class Attachment extends BaseEntity {
    @Column(name = "storage_key") private String storageKey;
    @Column(name = "file_name") private String fileName;
    @Column(name = "content_type") private String contentType;
    @Column(name = "size_bytes") private Long sizeBytes;
    @Column(name = "checksum") private String checksum;
    @Lob @Column(name = "metadata_json") private String metadataJson;
    @Column(name = "created_by") private UUID createdBy;
    @Column(name = "status") private String status;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "attachment_link", indexes = @Index(name = "idx_attachment_link_business", columnList = "business_id"))
class AttachmentLink extends BaseEntity {
    @Column(name = "attachment_id") private UUID attachmentId;
    @Column(name = "subject_type") private String subjectType;
    @Column(name = "subject_id") private UUID subjectId;
    @Column(name = "purpose") private String purpose;
}
