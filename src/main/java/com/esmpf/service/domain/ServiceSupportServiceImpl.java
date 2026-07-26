package com.esmpf.service.domain;

import static com.esmpf.service.ServiceSupportDtos.*;

import com.esmpf.customer.CustomerReferenceQuery;
import com.esmpf.document.DocumentReferenceQuery;
import com.esmpf.equipment.EquipmentReferenceQuery;
import com.esmpf.identity.IdentityReferenceQuery;
import com.esmpf.service.ServiceReferenceQuery;
import com.esmpf.service.ServiceSupportService;
import com.esmpf.shared.exception.EntityNotFoundException;
import com.esmpf.shared.exception.StaleEntityException;
import com.esmpf.shared.tenant.TenantContext;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class ServiceSupportServiceImpl implements ServiceSupportService {
    private final TenantContext tenantContext;
    private final CustomerReferenceQuery customerReferences;
    private final EquipmentReferenceQuery equipmentReferences;
    private final ServiceReferenceQuery serviceReferences;
    private final IdentityReferenceQuery identityReferences;
    private final DocumentReferenceQuery documentReferences;
    private final RecommendationRepository recommendationRepository;
    private final MaterialCatalogItemRepository materialRepository;
    private final JobMaterialRepository jobMaterialRepository;
    private final ServiceAgreementRepository agreementRepository;
    private final WarrantyCaseRepository warrantyRepository;
    private final MobileDeviceRepository deviceRepository;
    private final SyncOperationRepository syncRepository;

    @Override @Transactional
    public RecommendationResponse createRecommendation(RecommendationCommand c) {
        equipmentReferences.requireEquipment(c.equipmentId()); if (c.sourceJobId() != null) serviceReferences.requireJob(c.sourceJobId());
        Recommendation e = new Recommendation(); e.setBusinessId(tenant()); e.setEquipmentId(c.equipmentId()); e.setSourceJobId(c.sourceJobId()); e.setDescription(c.description()); e.setPriority(c.priority()); e.setDueDate(c.dueDate()); e.setStatus("OPEN"); return response(recommendationRepository.saveAndFlush(e));
    }
    @Override @Transactional public RecommendationResponse convertRecommendation(UUID id, long version, UUID jobId) { Recommendation e = requireRecommendation(id); checkVersion("Recommendation", id, version, e.getVersion()); requireStatus(e.getStatus(), "OPEN"); serviceReferences.requireJob(jobId); e.setConvertedJobId(jobId); e.setStatus("CONVERTED"); return response(recommendationRepository.saveAndFlush(e)); }
    @Override @Transactional public RecommendationResponse dismissRecommendation(UUID id, long version) { Recommendation e = requireRecommendation(id); checkVersion("Recommendation", id, version, e.getVersion()); requireStatus(e.getStatus(), "OPEN"); e.setStatus("DISMISSED"); return response(recommendationRepository.saveAndFlush(e)); }
    @Override @Transactional(readOnly = true) public Page<RecommendationResponse> listRecommendations(UUID equipmentId, Pageable p) { equipmentReferences.requireEquipment(equipmentId); return recommendationRepository.findAllByBusinessIdAndEquipmentId(tenant(), equipmentId, p).map(entity -> response(entity)); }

    @Override @Transactional
    public MaterialCatalogResponse createMaterial(MaterialCatalogCommand c) { validateMaterial(c.defaultPrice(), c.currency()); if (materialRepository.existsByBusinessIdAndCodeIgnoreCase(tenant(), c.code())) throw new IllegalArgumentException("Material code already exists"); MaterialCatalogItem e = new MaterialCatalogItem(); e.setBusinessId(tenant()); apply(c, e); e.setActive(true); return response(materialRepository.saveAndFlush(e)); }
    @Override @Transactional public MaterialCatalogResponse updateMaterial(UUID id, MaterialCatalogCommand c) { MaterialCatalogItem e = requireMaterial(id); checkVersion("MaterialCatalogItem", id, c.version(), e.getVersion()); validateMaterial(c.defaultPrice(), c.currency()); e.setName(c.name()); e.setUnitCode(c.unitCode()); e.setDefaultPrice(c.defaultPrice()); e.setCurrency(c.currency()); return response(materialRepository.saveAndFlush(e)); }
    @Override @Transactional public MaterialCatalogResponse deactivateMaterial(UUID id, long version) { MaterialCatalogItem e = requireMaterial(id); checkVersion("MaterialCatalogItem", id, version, e.getVersion()); e.setActive(false); return response(materialRepository.saveAndFlush(e)); }
    @Override @Transactional(readOnly = true) public Page<MaterialCatalogResponse> listMaterials(Pageable p) { return materialRepository.findAllByBusinessId(tenant(), p).map(entity -> response(entity)); }

    @Override @Transactional
    public JobMaterialResponse addJobMaterial(JobMaterialCommand c) { serviceReferences.requireJob(c.jobId()); if (c.materialCatalogItemId() != null) { MaterialCatalogItem item = requireMaterial(c.materialCatalogItemId()); if (!Boolean.TRUE.equals(item.getActive())) throw new IllegalStateException("Material is inactive"); } if (c.quantity() == null || c.quantity().signum() <= 0) throw new IllegalArgumentException("quantity must be positive"); if (c.unitPrice() != null && c.unitPrice().signum() < 0) throw new IllegalArgumentException("unitPrice must be non-negative"); JobMaterial e = new JobMaterial(); e.setBusinessId(tenant()); e.setJobId(c.jobId()); e.setMaterialCatalogItemId(c.materialCatalogItemId()); e.setType(c.type()); e.setDescription(c.description()); e.setQuantity(c.quantity()); e.setUnitCode(c.unitCode()); e.setUnitPrice(c.unitPrice()); e.setCurrency(c.currency()); e.setSource(c.source()); return response(jobMaterialRepository.saveAndFlush(e)); }
    @Override @Transactional(readOnly = true) public Page<JobMaterialResponse> listJobMaterials(UUID jobId, Pageable p) { serviceReferences.requireJob(jobId); return jobMaterialRepository.findAllByBusinessIdAndJobId(tenant(), jobId, p).map(entity -> response(entity)); }

    @Override @Transactional
    public ServiceAgreementResponse createAgreement(ServiceAgreementCommand c) { customerReferences.requireCustomer(c.customerId()); validateDates(c.validFrom(), c.validUntil()); if (c.attachmentId() != null) documentReferences.requireAttachment(c.attachmentId()); if (agreementRepository.existsByBusinessIdAndNumberIgnoreCase(tenant(), c.number())) throw new IllegalArgumentException("Agreement number already exists"); ServiceAgreement e = new ServiceAgreement(); e.setBusinessId(tenant()); apply(c, e); e.setStatus("DRAFT"); return response(agreementRepository.saveAndFlush(e)); }
    @Override @Transactional public ServiceAgreementResponse updateDraftAgreement(UUID id, ServiceAgreementCommand c) { ServiceAgreement e = requireAgreement(id); checkVersion("ServiceAgreement", id, c.version(), e.getVersion()); requireStatus(e.getStatus(), "DRAFT"); if (c.customerId() != null && !c.customerId().equals(e.getCustomerId())) throw new IllegalArgumentException("Agreement customer cannot change"); validateDates(c.validFrom(), c.validUntil()); if (c.attachmentId() != null) documentReferences.requireAttachment(c.attachmentId()); apply(c, e); return response(agreementRepository.saveAndFlush(e)); }
    @Override @Transactional public ServiceAgreementResponse activateAgreement(UUID id, long version) { return transitionAgreement(id, version, "DRAFT", "ACTIVE"); }
    @Override @Transactional public ServiceAgreementResponse suspendAgreement(UUID id, long version) { return transitionAgreement(id, version, "ACTIVE", "SUSPENDED"); }
    @Override @Transactional public ServiceAgreementResponse closeAgreement(UUID id, long version) { ServiceAgreement e = requireAgreement(id); checkVersion("ServiceAgreement", id, version, e.getVersion()); if ("CLOSED".equals(e.getStatus())) throw new IllegalStateException("Agreement already closed"); e.setStatus("CLOSED"); return response(agreementRepository.saveAndFlush(e)); }
    @Override @Transactional(readOnly = true) public Page<ServiceAgreementResponse> listAgreements(UUID customerId, Pageable p) { customerReferences.requireCustomer(customerId); return agreementRepository.findAllByBusinessIdAndCustomerId(tenant(), customerId, p).map(entity -> response(entity)); }

    @Override @Transactional
    public WarrantyCaseResponse openWarrantyCase(WarrantyCaseCommand c) { equipmentReferences.requireEquipment(c.equipmentId()); if (c.jobId() != null) serviceReferences.requireJob(c.jobId()); WarrantyCase e = new WarrantyCase(); e.setBusinessId(tenant()); e.setEquipmentId(c.equipmentId()); e.setJobId(c.jobId()); e.setSource(c.source()); e.setDescription(c.description()); e.setStatus("OPEN"); e.setOpenedAt(Instant.now()); return response(warrantyRepository.saveAndFlush(e)); }
    @Override @Transactional public WarrantyCaseResponse approveWarrantyCase(UUID id, long version, String decision) { return decideWarranty(id, version, "APPROVED", decision); }
    @Override @Transactional public WarrantyCaseResponse rejectWarrantyCase(UUID id, long version, String decision) { return decideWarranty(id, version, "REJECTED", decision); }
    @Override @Transactional public WarrantyCaseResponse closeWarrantyCase(UUID id, long version) { WarrantyCase e = requireWarranty(id); checkVersion("WarrantyCase", id, version, e.getVersion()); if (!("APPROVED".equals(e.getStatus()) || "REJECTED".equals(e.getStatus()))) throw new IllegalStateException("Warranty decision required before close"); e.setStatus("CLOSED"); e.setResolvedAt(Instant.now()); return response(warrantyRepository.saveAndFlush(e)); }
    @Override @Transactional(readOnly = true) public Page<WarrantyCaseResponse> listWarrantyCases(UUID equipmentId, Pageable p) { equipmentReferences.requireEquipment(equipmentId); return warrantyRepository.findAllByBusinessIdAndEquipmentId(tenant(), equipmentId, p).map(entity -> response(entity)); }

    @Override @Transactional
    public MobileDeviceResponse registerDevice(MobileDeviceCommand c) { var user = identityReferences.requireUser(c.userId()); if (!user.active()) throw new IllegalStateException("User is inactive"); if (deviceRepository.existsByBusinessIdAndDeviceIdentifier(tenant(), c.deviceIdentifier())) throw new IllegalArgumentException("Device already registered"); MobileDevice e = new MobileDevice(); e.setBusinessId(tenant()); e.setUserId(c.userId()); e.setDeviceIdentifier(c.deviceIdentifier()); e.setPlatform(c.platform()); e.setAppVersion(c.appVersion()); e.setStatus("ACTIVE"); e.setRegisteredAt(Instant.now()); e.setLastSeenAt(Instant.now()); return response(deviceRepository.saveAndFlush(e)); }
    @Override @Transactional public MobileDeviceResponse touchDevice(UUID id, long version, String appVersion) { MobileDevice e = requireDevice(id); checkVersion("MobileDevice", id, version, e.getVersion()); requireStatus(e.getStatus(), "ACTIVE"); e.setLastSeenAt(Instant.now()); e.setAppVersion(appVersion); return response(deviceRepository.saveAndFlush(e)); }
    @Override @Transactional public MobileDeviceResponse revokeDevice(UUID id, long version) { MobileDevice e = requireDevice(id); checkVersion("MobileDevice", id, version, e.getVersion()); e.setStatus("REVOKED"); return response(deviceRepository.saveAndFlush(e)); }
    @Override @Transactional(readOnly = true) public Page<MobileDeviceResponse> listDevices(UUID userId, Pageable p) { identityReferences.requireUser(userId); return deviceRepository.findAllByBusinessIdAndUserId(tenant(), userId, p).map(entity -> response(entity)); }

    @Override @Transactional
    public SyncOperationResponse receiveSyncOperation(SyncOperationCommand c) { MobileDevice device = requireDevice(c.deviceId()); requireStatus(device.getStatus(), "ACTIVE"); var existing = syncRepository.findByBusinessIdAndDeviceIdAndClientOperationId(tenant(), c.deviceId(), c.clientOperationId()); if (existing.isPresent()) { SyncOperation operation = existing.get(); if (!operation.getPayloadHash().equals(c.payloadHash())) throw new IllegalArgumentException("Client operation id reused with different payload"); return response(operation); } SyncOperation e = new SyncOperation(); e.setBusinessId(tenant()); e.setDeviceId(c.deviceId()); e.setClientOperationId(c.clientOperationId()); e.setOperationType(c.operationType()); e.setSubjectType(c.subjectType()); e.setSubjectId(c.subjectId()); e.setPayloadHash(c.payloadHash()); e.setStatus("RECEIVED"); e.setOccurredAt(c.occurredAt()); e.setReceivedAt(Instant.now()); return response(syncRepository.saveAndFlush(e)); }
    @Override @Transactional public SyncOperationResponse completeSyncOperation(UUID id, long version) { return transitionSync(id, version, "COMPLETED", null); }
    @Override @Transactional public SyncOperationResponse failSyncOperation(UUID id, long version, String error) { return transitionSync(id, version, "FAILED", error); }
    @Override @Transactional(readOnly = true) public Page<SyncOperationResponse> listSyncOperations(UUID deviceId, Pageable p) { requireDevice(deviceId); return syncRepository.findAllByBusinessIdAndDeviceId(tenant(), deviceId, p).map(entity -> response(entity)); }

    private static void apply(MaterialCatalogCommand c, MaterialCatalogItem e) { e.setCode(c.code()); e.setName(c.name()); e.setUnitCode(c.unitCode()); e.setDefaultPrice(c.defaultPrice()); e.setCurrency(c.currency()); }
    private static void apply(ServiceAgreementCommand c, ServiceAgreement e) { if (c.customerId() != null) e.setCustomerId(c.customerId()); if (c.number() != null) e.setNumber(c.number()); if (c.type() != null) e.setType(c.type()); e.setValidFrom(c.validFrom()); e.setValidUntil(c.validUntil()); e.setCoveredEquipmentIdsJson(c.coveredEquipmentIdsJson()); e.setCoverageRulesJson(c.coverageRulesJson()); e.setSlaRulesJson(c.slaRulesJson()); e.setPricingRulesJson(c.pricingRulesJson()); e.setAttachmentId(c.attachmentId()); }
    private Recommendation requireRecommendation(UUID id) { return recommendationRepository.findByIdAndBusinessId(id, tenant()).orElseThrow(() -> new EntityNotFoundException("Recommendation", id)); }
    private MaterialCatalogItem requireMaterial(UUID id) { return materialRepository.findByIdAndBusinessId(id, tenant()).orElseThrow(() -> new EntityNotFoundException("MaterialCatalogItem", id)); }
    private ServiceAgreement requireAgreement(UUID id) { return agreementRepository.findByIdAndBusinessId(id, tenant()).orElseThrow(() -> new EntityNotFoundException("ServiceAgreement", id)); }
    private WarrantyCase requireWarranty(UUID id) { return warrantyRepository.findByIdAndBusinessId(id, tenant()).orElseThrow(() -> new EntityNotFoundException("WarrantyCase", id)); }
    private MobileDevice requireDevice(UUID id) { return deviceRepository.findByIdAndBusinessId(id, tenant()).orElseThrow(() -> new EntityNotFoundException("MobileDevice", id)); }
    private SyncOperation requireSync(UUID id) { return syncRepository.findByIdAndBusinessId(id, tenant()).orElseThrow(() -> new EntityNotFoundException("SyncOperation", id)); }
    private ServiceAgreementResponse transitionAgreement(UUID id, long version, String from, String to) { ServiceAgreement e = requireAgreement(id); checkVersion("ServiceAgreement", id, version, e.getVersion()); requireStatus(e.getStatus(), from); e.setStatus(to); return response(agreementRepository.saveAndFlush(e)); }
    private WarrantyCaseResponse decideWarranty(UUID id, long version, String status, String decision) { WarrantyCase e = requireWarranty(id); checkVersion("WarrantyCase", id, version, e.getVersion()); requireStatus(e.getStatus(), "OPEN"); e.setStatus(status); e.setDecision(decision); return response(warrantyRepository.saveAndFlush(e)); }
    private SyncOperationResponse transitionSync(UUID id, long version, String status, String error) { SyncOperation e = requireSync(id); checkVersion("SyncOperation", id, version, e.getVersion()); requireStatus(e.getStatus(), "RECEIVED"); e.setStatus(status); e.setErrorCode(error); return response(syncRepository.saveAndFlush(e)); }
    private UUID tenant() { return tenantContext.requireBusinessId(); }
    private static void validateMaterial(BigDecimal price, String currency) { if (price != null && price.signum() < 0) throw new IllegalArgumentException("defaultPrice must be non-negative"); if (price != null && (currency == null || currency.length() != 3)) throw new IllegalArgumentException("Currency required for priced material"); }
    private static void validateDates(java.time.LocalDate from, java.time.LocalDate until) { if (from != null && until != null && until.isBefore(from)) throw new IllegalArgumentException("validUntil must not be before validFrom"); }
    private static void requireStatus(String actual, String expected) { if (!expected.equals(actual)) throw new IllegalStateException("Expected status " + expected + " but was " + actual); }
    private static void checkVersion(String type, UUID id, long expected, long actual) { if (expected != actual) throw new StaleEntityException(type, id, expected, actual); }
    private static RecommendationResponse response(Recommendation e) { return new RecommendationResponse(e.getId(), e.getVersion(), e.getEquipmentId(), e.getSourceJobId(), e.getDescription(), e.getPriority(), e.getDueDate(), e.getStatus(), e.getConvertedJobId(), e.getCreatedAt(), e.getUpdatedAt()); }
    private static MaterialCatalogResponse response(MaterialCatalogItem e) { return new MaterialCatalogResponse(e.getId(), e.getVersion(), e.getCode(), e.getName(), e.getUnitCode(), e.getDefaultPrice(), e.getCurrency(), Boolean.TRUE.equals(e.getActive()), e.getCreatedAt(), e.getUpdatedAt()); }
    private static JobMaterialResponse response(JobMaterial e) { return new JobMaterialResponse(e.getId(), e.getVersion(), e.getJobId(), e.getMaterialCatalogItemId(), e.getType(), e.getDescription(), e.getQuantity(), e.getUnitCode(), e.getUnitPrice(), e.getCurrency(), e.getSource(), e.getCreatedAt(), e.getUpdatedAt()); }
    private static ServiceAgreementResponse response(ServiceAgreement e) { return new ServiceAgreementResponse(e.getId(), e.getVersion(), e.getCustomerId(), e.getNumber(), e.getType(), e.getStatus(), e.getValidFrom(), e.getValidUntil(), e.getCoveredEquipmentIdsJson(), e.getCoverageRulesJson(), e.getSlaRulesJson(), e.getPricingRulesJson(), e.getAttachmentId(), e.getCreatedAt(), e.getUpdatedAt()); }
    private static WarrantyCaseResponse response(WarrantyCase e) { return new WarrantyCaseResponse(e.getId(), e.getVersion(), e.getEquipmentId(), e.getJobId(), e.getSource(), e.getStatus(), e.getDescription(), e.getDecision(), e.getOpenedAt(), e.getResolvedAt(), e.getCreatedAt(), e.getUpdatedAt()); }
    private static MobileDeviceResponse response(MobileDevice e) { return new MobileDeviceResponse(e.getId(), e.getVersion(), e.getUserId(), e.getDeviceIdentifier(), e.getPlatform(), e.getAppVersion(), e.getStatus(), e.getLastSeenAt(), e.getRegisteredAt(), e.getCreatedAt(), e.getUpdatedAt()); }
    private static SyncOperationResponse response(SyncOperation e) { return new SyncOperationResponse(e.getId(), e.getVersion(), e.getDeviceId(), e.getClientOperationId(), e.getOperationType(), e.getSubjectType(), e.getSubjectId(), e.getPayloadHash(), e.getStatus(), e.getOccurredAt(), e.getReceivedAt(), e.getErrorCode(), e.getCreatedAt(), e.getUpdatedAt()); }
}