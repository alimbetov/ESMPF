package com.esmpf.equipment.domain;

import static com.esmpf.equipment.EquipmentDtos.*;

import com.esmpf.catalog.CatalogDtos.EquipmentTypeReference;
import com.esmpf.catalog.CatalogReferenceQuery;
import com.esmpf.customer.CustomerDtos.CustomerReference;
import com.esmpf.customer.CustomerDtos.ServiceLocationReference;
import com.esmpf.customer.CustomerReferenceQuery;
import com.esmpf.equipment.EquipmentReferenceQuery;
import com.esmpf.equipment.EquipmentService;
import com.esmpf.shared.exception.EntityNotFoundException;
import com.esmpf.shared.exception.StaleEntityException;
import com.esmpf.shared.tenant.TenantContext;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class EquipmentServiceImpl implements EquipmentService, EquipmentReferenceQuery {

    private final TenantContext tenantContext;
    private final CustomerReferenceQuery customerReferences;
    private final CatalogReferenceQuery catalogReferences;
    private final EquipmentRepository equipmentRepository;
    private final EquipmentRelationRepository relationRepository;
    private final EquipmentIssueRepository issueRepository;
    private final MeterReadingRepository meterReadingRepository;
    private final EquipmentMapper mapper;

    @Override
    @Transactional
    public EquipmentResponse createEquipment(EquipmentCreateCommand command) {
        UUID tenant = tenant();
        CustomerReference customer = customerReferences.requireCustomer(command.customerId());
        requireActive(customer.status(), "Customer");
        ServiceLocationReference location = customerReferences.requireServiceLocation(command.serviceLocationId());
        validateLocationCustomer(location, command.customerId());
        requireActive(location.status(), "ServiceLocation");
        EquipmentTypeReference type = catalogReferences.requireEquipmentType(command.equipmentTypeId());
        requireActive(type.status(), "EquipmentType");
        validateParent(command.parentEquipmentId(), command.customerId(), null);
        validateUniqueIdentifiers(command.serialNumber(), command.assetNumber(), null);
        validateDates(command.installationDate(), command.commissioningDate(), command.warrantyUntil());

        Equipment entity = mapper.toEntity(command);
        entity.setBusinessId(tenant);
        return mapper.toResponse(equipmentRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public EquipmentResponse getEquipment(UUID id) {
        return mapper.toResponse(requireEquipmentEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EquipmentResponse> listEquipment(Pageable pageable) {
        return equipmentRepository.findAllByBusinessId(tenant(), pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public EquipmentResponse updateEquipment(UUID id, EquipmentUpdateCommand command) {
        Equipment entity = requireEquipmentEntity(id);
        checkVersion("Equipment", id, command.version(), entity.getVersion());
        requireActive(entity.getStatus(), "Equipment");

        UUID locationId = command.serviceLocationId() == null
                ? entity.getServiceLocationId()
                : command.serviceLocationId();
        ServiceLocationReference location = customerReferences.requireServiceLocation(locationId);
        validateLocationCustomer(location, entity.getCustomerId());
        requireActive(location.status(), "ServiceLocation");

        validateParent(command.parentEquipmentId(), entity.getCustomerId(), entity.getId());
        validateUniqueIdentifiers(command.serialNumber(), command.assetNumber(), entity);
        validateDates(
                command.installationDate() == null ? entity.getInstallationDate() : command.installationDate(),
                command.commissioningDate() == null ? entity.getCommissioningDate() : command.commissioningDate(),
                command.warrantyUntil() == null ? entity.getWarrantyUntil() : command.warrantyUntil());
        mapper.update(command, entity);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public EquipmentResponse archiveEquipment(UUID id, long version) {
        Equipment entity = requireEquipmentEntity(id);
        checkVersion("Equipment", id, version, entity.getVersion());
        entity.setStatus("ARCHIVED");
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public EquipmentRelationResponse createRelation(EquipmentRelationCreateCommand command) {
        if (command.sourceEquipmentId().equals(command.targetEquipmentId())) {
            throw new IllegalArgumentException("Equipment cannot relate to itself");
        }
        Equipment source = requireEquipmentEntity(command.sourceEquipmentId());
        Equipment target = requireEquipmentEntity(command.targetEquipmentId());
        requireActive(source.getStatus(), "Source equipment");
        requireActive(target.getStatus(), "Target equipment");
        if (!source.getCustomerId().equals(target.getCustomerId())) {
            throw new IllegalArgumentException("Related equipment must belong to the same customer");
        }
        if (command.validFrom() != null && command.validUntil() != null
                && command.validUntil().isBefore(command.validFrom())) {
            throw new IllegalArgumentException("Relation validUntil cannot be before validFrom");
        }
        if (relationRepository.existsByBusinessIdAndSourceEquipmentIdAndTargetEquipmentIdAndRelationTypeAndValidUntilIsNull(
                tenant(), command.sourceEquipmentId(), command.targetEquipmentId(), command.relationType())) {
            throw new IllegalArgumentException("Active equipment relation already exists");
        }
        EquipmentRelation entity = mapper.toEntity(command);
        entity.setBusinessId(tenant());
        if (entity.getValidFrom() == null) {
            entity.setValidFrom(LocalDate.now());
        }
        return mapper.toResponse(relationRepository.save(entity));
    }

    @Override
    @Transactional
    public EquipmentRelationResponse endRelation(UUID relationId, long version) {
        EquipmentRelation entity = requireRelationEntity(relationId);
        checkVersion("EquipmentRelation", relationId, version, entity.getVersion());
        if (entity.getValidUntil() != null) {
            throw new IllegalStateException("Equipment relation is already ended");
        }
        entity.setValidUntil(LocalDate.now());
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EquipmentRelationResponse> listRelations(UUID equipmentId, Pageable pageable) {
        requireEquipmentEntity(equipmentId);
        return relationRepository.findAllForEquipment(tenant(), equipmentId, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public EquipmentIssueResponse reportIssue(EquipmentIssueCreateCommand command) {
        Equipment equipment = requireEquipmentEntity(command.equipmentId());
        requireActive(equipment.getStatus(), "Equipment");
        EquipmentIssue entity = mapper.toEntity(command);
        entity.setBusinessId(tenant());
        entity.setDetectedAt(Instant.now());
        return mapper.toResponse(issueRepository.save(entity));
    }

    @Override
    @Transactional
    public EquipmentIssueResponse resolveIssue(UUID issueId, long version, UUID resolvedByJobId) {
        EquipmentIssue entity = requireIssueEntity(issueId);
        checkVersion("EquipmentIssue", issueId, version, entity.getVersion());
        if (!"OPEN".equals(entity.getStatus())) {
            throw new IllegalStateException("Only OPEN equipment issues can be resolved");
        }
        entity.setStatus("RESOLVED");
        entity.setResolvedByJobId(resolvedByJobId);
        entity.setResolvedAt(Instant.now());
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EquipmentIssueResponse> listIssues(UUID equipmentId, Pageable pageable) {
        requireEquipmentEntity(equipmentId);
        return issueRepository.findAllByBusinessIdAndEquipmentId(tenant(), equipmentId, pageable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional
    public MeterReadingResponse recordMeterReading(MeterReadingCommand command) {
        Equipment equipment = requireEquipmentEntity(command.equipmentId());
        requireActive(equipment.getStatus(), "Equipment");
        if (command.readingValue().signum() < 0) {
            throw new IllegalArgumentException("Meter reading cannot be negative");
        }
        MeterReading entity = mapper.toEntity(command);
        entity.setBusinessId(tenant());
        entity.setRecordedBy(tenantContext.requireUserId());
        if (entity.getRecordedAt() == null) {
            entity.setRecordedAt(Instant.now());
        }
        return mapper.toResponse(meterReadingRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MeterReadingResponse> listMeterReadings(UUID equipmentId, Pageable pageable) {
        requireEquipmentEntity(equipmentId);
        return meterReadingRepository.findAllByBusinessIdAndEquipmentId(tenant(), equipmentId, pageable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public EquipmentReference requireEquipment(UUID equipmentId) {
        Equipment entity = requireEquipmentEntity(equipmentId);
        return new EquipmentReference(
                entity.getId(), entity.getCustomerId(), entity.getServiceLocationId(),
                entity.getEquipmentTypeId(), entity.getName(), entity.getStatus());
    }

    private UUID tenant() {
        return tenantContext.requireBusinessId();
    }

    private Equipment requireEquipmentEntity(UUID id) {
        return equipmentRepository.findByIdAndBusinessId(id, tenant())
                .orElseThrow(() -> new EntityNotFoundException("Equipment", id));
    }

    private EquipmentRelation requireRelationEntity(UUID id) {
        return relationRepository.findByIdAndBusinessId(id, tenant())
                .orElseThrow(() -> new EntityNotFoundException("EquipmentRelation", id));
    }

    private EquipmentIssue requireIssueEntity(UUID id) {
        return issueRepository.findByIdAndBusinessId(id, tenant())
                .orElseThrow(() -> new EntityNotFoundException("EquipmentIssue", id));
    }

    private void validateParent(UUID parentId, UUID customerId, UUID currentId) {
        if (parentId == null) {
            return;
        }
        if (parentId.equals(currentId)) {
            throw new IllegalArgumentException("Equipment cannot be its own parent");
        }
        Equipment parent = requireEquipmentEntity(parentId);
        requireActive(parent.getStatus(), "Parent equipment");
        if (!parent.getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("Parent equipment belongs to another customer");
        }
    }

    private void validateUniqueIdentifiers(String serialNumber, String assetNumber, Equipment current) {
        if (serialNumber != null && !serialNumber.isBlank()
                && (current == null || current.getSerialNumber() == null
                    || !current.getSerialNumber().equalsIgnoreCase(serialNumber))
                && equipmentRepository.existsByBusinessIdAndSerialNumberIgnoreCase(tenant(), serialNumber)) {
            throw new IllegalArgumentException("Equipment serial number already exists in this tenant");
        }
        if (assetNumber != null && !assetNumber.isBlank()
                && (current == null || current.getAssetNumber() == null
                    || !current.getAssetNumber().equalsIgnoreCase(assetNumber))
                && equipmentRepository.existsByBusinessIdAndAssetNumberIgnoreCase(tenant(), assetNumber)) {
            throw new IllegalArgumentException("Equipment asset number already exists in this tenant");
        }
    }

    private static void validateLocationCustomer(ServiceLocationReference location, UUID customerId) {
        if (!location.customerId().equals(customerId)) {
            throw new IllegalArgumentException("Service location belongs to another customer");
        }
    }

    private static void validateDates(LocalDate installation, LocalDate commissioning, LocalDate warrantyUntil) {
        if (installation != null && commissioning != null && commissioning.isBefore(installation)) {
            throw new IllegalArgumentException("Commissioning date cannot be before installation date");
        }
        if (commissioning != null && warrantyUntil != null && warrantyUntil.isBefore(commissioning)) {
            throw new IllegalArgumentException("Warranty end cannot be before commissioning date");
        }
    }

    private static void checkVersion(String name, UUID id, long expected, long actual) {
        if (expected != actual) {
            throw new StaleEntityException(name, id, expected, actual);
        }
    }

    private static void requireActive(String status, String entity) {
        if (!"ACTIVE".equals(status)) {
            throw new IllegalStateException(entity + " is not active");
        }
    }
}
