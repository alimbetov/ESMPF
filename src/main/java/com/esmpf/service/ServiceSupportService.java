package com.esmpf.service;

import static com.esmpf.service.ServiceSupportDtos.*;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ServiceSupportService {
    RecommendationResponse createRecommendation(RecommendationCommand command);
    RecommendationResponse convertRecommendation(UUID recommendationId, long version, UUID jobId);
    RecommendationResponse dismissRecommendation(UUID recommendationId, long version);
    Page<RecommendationResponse> listRecommendations(UUID equipmentId, Pageable pageable);

    MaterialCatalogResponse createMaterial(MaterialCatalogCommand command);
    MaterialCatalogResponse getMaterial(UUID materialId);
    MaterialCatalogResponse updateMaterial(UUID materialId, MaterialCatalogCommand command);
    MaterialCatalogResponse deactivateMaterial(UUID materialId, long version);
    Page<MaterialCatalogResponse> listMaterials(Pageable pageable);

    JobMaterialResponse addJobMaterial(JobMaterialCommand command);
    Page<JobMaterialResponse> listJobMaterials(UUID jobId, Pageable pageable);

    ServiceAgreementResponse createAgreement(ServiceAgreementCommand command);
    ServiceAgreementResponse getAgreement(UUID agreementId);
    ServiceAgreementResponse updateDraftAgreement(UUID agreementId, ServiceAgreementCommand command);
    ServiceAgreementResponse activateAgreement(UUID agreementId, long version);
    ServiceAgreementResponse suspendAgreement(UUID agreementId, long version);
    ServiceAgreementResponse closeAgreement(UUID agreementId, long version);
    Page<ServiceAgreementResponse> listAgreements(UUID customerId, Pageable pageable);

    WarrantyCaseResponse openWarrantyCase(WarrantyCaseCommand command);
    WarrantyCaseResponse getWarrantyCase(UUID caseId);
    WarrantyCaseResponse approveWarrantyCase(UUID caseId, long version, String decision);
    WarrantyCaseResponse rejectWarrantyCase(UUID caseId, long version, String decision);
    WarrantyCaseResponse closeWarrantyCase(UUID caseId, long version);
    Page<WarrantyCaseResponse> listWarrantyCases(UUID equipmentId, Pageable pageable);

    MobileDeviceResponse registerDevice(MobileDeviceCommand command);
    MobileDeviceResponse getDevice(UUID deviceId);
    MobileDeviceResponse touchDevice(UUID deviceId, long version, String appVersion);
    MobileDeviceResponse revokeDevice(UUID deviceId, long version);
    Page<MobileDeviceResponse> listDevices(UUID userId, Pageable pageable);

    SyncOperationResponse receiveSyncOperation(SyncOperationCommand command);
    SyncOperationResponse completeSyncOperation(UUID operationId, long version);
    SyncOperationResponse failSyncOperation(UUID operationId, long version, String errorCode);
    Page<SyncOperationResponse> listSyncOperations(UUID deviceId, Pageable pageable);
}
