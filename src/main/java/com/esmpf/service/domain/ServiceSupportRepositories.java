package com.esmpf.service.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface RecommendationRepository extends JpaRepository<Recommendation, UUID> {
    Optional<Recommendation> findByIdAndBusinessId(UUID id, UUID businessId);
    Page<Recommendation> findAllByBusinessIdAndEquipmentId(UUID businessId, UUID equipmentId, Pageable pageable);
}
interface MaterialCatalogItemRepository extends JpaRepository<MaterialCatalogItem, UUID> {
    Optional<MaterialCatalogItem> findByIdAndBusinessId(UUID id, UUID businessId);
    Page<MaterialCatalogItem> findAllByBusinessId(UUID businessId, Pageable pageable);
    boolean existsByBusinessIdAndCodeIgnoreCase(UUID businessId, String code);
}
interface JobMaterialRepository extends JpaRepository<JobMaterial, UUID> {
    Page<JobMaterial> findAllByBusinessIdAndJobId(UUID businessId, UUID jobId, Pageable pageable);
}
interface ServiceAgreementRepository extends JpaRepository<ServiceAgreement, UUID> {
    Optional<ServiceAgreement> findByIdAndBusinessId(UUID id, UUID businessId);
    Page<ServiceAgreement> findAllByBusinessIdAndCustomerId(UUID businessId, UUID customerId, Pageable pageable);
    boolean existsByBusinessIdAndNumberIgnoreCase(UUID businessId, String number);
}
interface WarrantyCaseRepository extends JpaRepository<WarrantyCase, UUID> {
    Optional<WarrantyCase> findByIdAndBusinessId(UUID id, UUID businessId);
    Page<WarrantyCase> findAllByBusinessIdAndEquipmentId(UUID businessId, UUID equipmentId, Pageable pageable);
}
interface MobileDeviceRepository extends JpaRepository<MobileDevice, UUID> {
    Optional<MobileDevice> findByIdAndBusinessId(UUID id, UUID businessId);
    Page<MobileDevice> findAllByBusinessIdAndUserId(UUID businessId, UUID userId, Pageable pageable);
    boolean existsByBusinessIdAndDeviceIdentifier(UUID businessId, String deviceIdentifier);
}
interface SyncOperationRepository extends JpaRepository<SyncOperation, UUID> {
    Optional<SyncOperation> findByIdAndBusinessId(UUID id, UUID businessId);
    Page<SyncOperation> findAllByBusinessIdAndDeviceId(UUID businessId, UUID deviceId, Pageable pageable);
    Optional<SyncOperation> findByBusinessIdAndDeviceIdAndClientOperationId(UUID businessId, UUID deviceId, String clientOperationId);
}