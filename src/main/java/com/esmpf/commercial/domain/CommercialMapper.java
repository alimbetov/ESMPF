package com.esmpf.commercial.domain;

import static com.esmpf.commercial.CommercialDtos.*;

import com.esmpf.shared.mapping.EsmpfMapperConfig;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = EsmpfMapperConfig.class)
interface CommercialMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", constant = "DRAFT")
    @Mapping(target = "approvalDataJson", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    Estimate toEntity(EstimateCreateCommand command);
    EstimateResponse toResponse(Estimate entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "jobId", ignore = true)
    @Mapping(target = "number", ignore = true)
    @Mapping(target = "currency", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "approvalDataJson", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    void update(EstimateUpdateCommand command, @MappingTarget Estimate entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", constant = "DRAFT")
    @Mapping(target = "paidAmount", expression = "java(java.math.BigDecimal.ZERO)")
    @Mapping(target = "externalAccountingId", ignore = true)
    @Mapping(target = "generatedDocumentId", ignore = true)
    Invoice toEntity(InvoiceCreateCommand command);
    InvoiceResponse toResponse(Invoice entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "paidAt", ignore = true)
    Payment toEntity(PaymentCreateCommand command);
    PaymentResponse toResponse(Payment entity);
}