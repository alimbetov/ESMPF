package com.esmpf.service;

import static com.esmpf.service.ServiceSupportDtos.*;
import static com.esmpf.web.ApiActionRequests.ReferenceRequest;
import static com.esmpf.web.ApiActionRequests.TextRequest;
import static com.esmpf.web.ApiActionRequests.VersionRequest;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ServiceSupportRestController {
    private final ServiceSupportService service;

    @PostMapping("/recommendations") @ResponseStatus(HttpStatus.CREATED)
    public RecommendationResponse createRecommendation(@Valid @RequestBody RecommendationCommand command) { return service.createRecommendation(command); }
    @PostMapping("/recommendations/{recommendationId}/actions/convert") public RecommendationResponse convertRecommendation(@PathVariable UUID recommendationId, @Valid @RequestBody ReferenceRequest request) { return service.convertRecommendation(recommendationId, request.version(), request.referenceId()); }
    @PostMapping("/recommendations/{recommendationId}/actions/dismiss") public RecommendationResponse dismissRecommendation(@PathVariable UUID recommendationId, @Valid @RequestBody VersionRequest request) { return service.dismissRecommendation(recommendationId, request.version()); }
    @GetMapping("/equipment/{equipmentId}/recommendations") public Page<RecommendationResponse> listRecommendations(@PathVariable UUID equipmentId, Pageable pageable) { return service.listRecommendations(equipmentId, pageable); }

    @PostMapping("/materials") @ResponseStatus(HttpStatus.CREATED)
    public MaterialCatalogResponse createMaterial(@Valid @RequestBody MaterialCatalogCommand command) { return service.createMaterial(command); }
    @GetMapping("/materials/{materialId}") public MaterialCatalogResponse getMaterial(@PathVariable UUID materialId) { return service.getMaterial(materialId); }
    @PutMapping("/materials/{materialId}") public MaterialCatalogResponse updateMaterial(@PathVariable UUID materialId, @Valid @RequestBody MaterialCatalogCommand command) { return service.updateMaterial(materialId, command); }
    @PostMapping("/materials/{materialId}/actions/deactivate") public MaterialCatalogResponse deactivateMaterial(@PathVariable UUID materialId, @Valid @RequestBody VersionRequest request) { return service.deactivateMaterial(materialId, request.version()); }
    @GetMapping("/materials") public Page<MaterialCatalogResponse> listMaterials(Pageable pageable) { return service.listMaterials(pageable); }

    @PostMapping("/job-materials") @ResponseStatus(HttpStatus.CREATED)
    public JobMaterialResponse addJobMaterial(@Valid @RequestBody JobMaterialCommand command) { return service.addJobMaterial(command); }
    @GetMapping("/service-jobs/{jobId}/materials") public Page<JobMaterialResponse> listJobMaterials(@PathVariable UUID jobId, Pageable pageable) { return service.listJobMaterials(jobId, pageable); }

    @PostMapping("/service-agreements") @ResponseStatus(HttpStatus.CREATED)
    public ServiceAgreementResponse createAgreement(@Valid @RequestBody ServiceAgreementCommand command) { return service.createAgreement(command); }
    @GetMapping("/service-agreements/{agreementId}") public ServiceAgreementResponse getAgreement(@PathVariable UUID agreementId) { return service.getAgreement(agreementId); }
    @PutMapping("/service-agreements/{agreementId}") public ServiceAgreementResponse updateDraftAgreement(@PathVariable UUID agreementId, @Valid @RequestBody ServiceAgreementCommand command) { return service.updateDraftAgreement(agreementId, command); }
    @PostMapping("/service-agreements/{agreementId}/actions/activate") public ServiceAgreementResponse activateAgreement(@PathVariable UUID agreementId, @Valid @RequestBody VersionRequest request) { return service.activateAgreement(agreementId, request.version()); }
    @PostMapping("/service-agreements/{agreementId}/actions/suspend") public ServiceAgreementResponse suspendAgreement(@PathVariable UUID agreementId, @Valid @RequestBody VersionRequest request) { return service.suspendAgreement(agreementId, request.version()); }
    @PostMapping("/service-agreements/{agreementId}/actions/close") public ServiceAgreementResponse closeAgreement(@PathVariable UUID agreementId, @Valid @RequestBody VersionRequest request) { return service.closeAgreement(agreementId, request.version()); }
    @GetMapping("/customers/{customerId}/service-agreements") public Page<ServiceAgreementResponse> listAgreements(@PathVariable UUID customerId, Pageable pageable) { return service.listAgreements(customerId, pageable); }

    @PostMapping("/warranty-cases") @ResponseStatus(HttpStatus.CREATED)
    public WarrantyCaseResponse openWarrantyCase(@Valid @RequestBody WarrantyCaseCommand command) { return service.openWarrantyCase(command); }
    @GetMapping("/warranty-cases/{caseId}") public WarrantyCaseResponse getWarrantyCase(@PathVariable UUID caseId) { return service.getWarrantyCase(caseId); }
    @PostMapping("/warranty-cases/{caseId}/actions/approve") public WarrantyCaseResponse approveWarrantyCase(@PathVariable UUID caseId, @Valid @RequestBody TextRequest request) { return service.approveWarrantyCase(caseId, request.version(), request.value()); }
    @PostMapping("/warranty-cases/{caseId}/actions/reject") public WarrantyCaseResponse rejectWarrantyCase(@PathVariable UUID caseId, @Valid @RequestBody TextRequest request) { return service.rejectWarrantyCase(caseId, request.version(), request.value()); }
    @PostMapping("/warranty-cases/{caseId}/actions/close") public WarrantyCaseResponse closeWarrantyCase(@PathVariable UUID caseId, @Valid @RequestBody VersionRequest request) { return service.closeWarrantyCase(caseId, request.version()); }
    @GetMapping("/equipment/{equipmentId}/warranty-cases") public Page<WarrantyCaseResponse> listWarrantyCases(@PathVariable UUID equipmentId, Pageable pageable) { return service.listWarrantyCases(equipmentId, pageable); }

    @PostMapping("/mobile-devices") @ResponseStatus(HttpStatus.CREATED)
    public MobileDeviceResponse registerDevice(@Valid @RequestBody MobileDeviceCommand command) { return service.registerDevice(command); }
    @GetMapping("/mobile-devices/{deviceId}") public MobileDeviceResponse getDevice(@PathVariable UUID deviceId) { return service.getDevice(deviceId); }
    @PostMapping("/mobile-devices/{deviceId}/actions/touch") public MobileDeviceResponse touchDevice(@PathVariable UUID deviceId, @Valid @RequestBody TextRequest request) { return service.touchDevice(deviceId, request.version(), request.value()); }
    @PostMapping("/mobile-devices/{deviceId}/actions/revoke") public MobileDeviceResponse revokeDevice(@PathVariable UUID deviceId, @Valid @RequestBody VersionRequest request) { return service.revokeDevice(deviceId, request.version()); }
    @GetMapping("/users/{userId}/mobile-devices") public Page<MobileDeviceResponse> listDevices(@PathVariable UUID userId, Pageable pageable) { return service.listDevices(userId, pageable); }

    @PostMapping("/sync-operations") @ResponseStatus(HttpStatus.CREATED)
    public SyncOperationResponse receiveSyncOperation(@Valid @RequestBody SyncOperationCommand command) { return service.receiveSyncOperation(command); }
    @PostMapping("/sync-operations/{operationId}/actions/complete") public SyncOperationResponse completeSyncOperation(@PathVariable UUID operationId, @Valid @RequestBody VersionRequest request) { return service.completeSyncOperation(operationId, request.version()); }
    @PostMapping("/sync-operations/{operationId}/actions/fail") public SyncOperationResponse failSyncOperation(@PathVariable UUID operationId, @Valid @RequestBody TextRequest request) { return service.failSyncOperation(operationId, request.version(), request.value()); }
    @GetMapping("/mobile-devices/{deviceId}/sync-operations") public Page<SyncOperationResponse> listSyncOperations(@PathVariable UUID deviceId, Pageable pageable) { return service.listSyncOperations(deviceId, pageable); }
}
