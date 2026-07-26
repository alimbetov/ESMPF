package com.esmpf.equipment.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface EquipmentRepository extends JpaRepository<Equipment, UUID> {
    Optional<Equipment> findByIdAndBusinessId(UUID id, UUID businessId);
    Page<Equipment> findAllByBusinessId(UUID businessId, Pageable pageable);
    boolean existsByBusinessIdAndSerialNumberIgnoreCase(UUID businessId, String serialNumber);
    boolean existsByBusinessIdAndAssetNumberIgnoreCase(UUID businessId, String assetNumber);
}

interface EquipmentRelationRepository extends JpaRepository<EquipmentRelation, UUID> {
    Optional<EquipmentRelation> findByIdAndBusinessId(UUID id, UUID businessId);

    @Query("""
            select relation from EquipmentRelation relation
            where relation.businessId = :businessId
              and (relation.sourceEquipmentId = :equipmentId
                   or relation.targetEquipmentId = :equipmentId)
            """)
    Page<EquipmentRelation> findAllForEquipment(
            @Param("businessId") UUID businessId,
            @Param("equipmentId") UUID equipmentId,
            Pageable pageable
    );

    boolean existsByBusinessIdAndSourceEquipmentIdAndTargetEquipmentIdAndRelationTypeAndValidUntilIsNull(
            UUID businessId, UUID sourceEquipmentId, UUID targetEquipmentId, String relationType);
}

interface EquipmentIssueRepository extends JpaRepository<EquipmentIssue, UUID> {
    Optional<EquipmentIssue> findByIdAndBusinessId(UUID id, UUID businessId);
    Page<EquipmentIssue> findAllByBusinessIdAndEquipmentId(UUID businessId, UUID equipmentId, Pageable pageable);
}

interface MeterReadingRepository extends JpaRepository<MeterReading, UUID> {
    Page<MeterReading> findAllByBusinessIdAndEquipmentId(UUID businessId, UUID equipmentId, Pageable pageable);
}
