package com.esmpf.document.domain;

import static com.esmpf.document.DocumentDtos.*;

import com.esmpf.shared.mapping.EsmpfMapperConfig;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = EsmpfMapperConfig.class)
interface DocumentMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", constant = "DRAFT")
    @Mapping(target = "publishedAt", ignore = true)
    ReportTemplate toEntity(ReportTemplateCommand command);
    ReportTemplateResponse toResponse(ReportTemplate entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "documentType", ignore = true)
    @Mapping(target = "locale", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    void update(ReportTemplateCommand command, @MappingTarget ReportTemplate entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", constant = "REQUESTED")
    @Mapping(target = "attachmentId", ignore = true)
    @Mapping(target = "checksum", ignore = true)
    @Mapping(target = "generationAttempts", constant = "0")
    @Mapping(target = "lastError", ignore = true)
    @Mapping(target = "generatedAt", ignore = true)
    @Mapping(target = "deliveryDataJson", ignore = true)
    GeneratedDocument toEntity(DocumentGenerationCommand command);
    GeneratedDocumentResponse toResponse(GeneratedDocument entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    Attachment toEntity(AttachmentCommand command);
    AttachmentResponse toResponse(Attachment entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    AttachmentLink toEntity(AttachmentLinkCommand command);
    AttachmentLinkResponse toResponse(AttachmentLink entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "signedAt", ignore = true)
    DocumentSignature toEntity(DocumentSignatureCommand command);
    DocumentSignatureResponse toResponse(DocumentSignature entity);
}