package com.esmpf.maintenance.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface MaintenancePlanRepository extends JpaRepository<MaintenancePlan, UUID> {
    Optional<MaintenancePlan> findByIdAndBusinessId(UUID id, UUID businessId);
    Page<MaintenancePlan> findAllByBusinessId(UUID businessId, Pageable pageable);
    boolean existsByBusinessIdAndEquipmentIdAndMaintenanceTemplateIdAndStatusIn(
            UUID businessId, UUID equipmentId, UUID maintenanceTemplateId, Iterable<String> statuses);
}

interface MaintenanceOccurrenceRepository extends JpaRepository<MaintenanceOccurrence, UUID> {
    Optional<MaintenanceOccurrence> findByIdAndBusinessId(UUID id, UUID businessId);
    Page<MaintenanceOccurrence> findAllByBusinessIdAndMaintenancePlanId(
            UUID businessId, UUID maintenancePlanId, Pageable pageable);
    boolean existsByBusinessIdAndMaintenancePlanIdAndGenerationKey(
            UUID businessId, UUID maintenancePlanId, String generationKey);
}
