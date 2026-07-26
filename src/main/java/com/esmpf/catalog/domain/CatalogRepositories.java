package com.esmpf.catalog.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface EquipmentTypeRepository extends JpaRepository<EquipmentType, UUID> {
    Optional<EquipmentType> findByIdAndBusinessId(UUID id, UUID businessId);
    Page<EquipmentType> findAllByBusinessId(UUID businessId, Pageable pageable);
    boolean existsByBusinessIdAndCodeIgnoreCase(UUID businessId, String code);
}

interface JobTypeRepository extends JpaRepository<JobType, UUID> {
    Optional<JobType> findByIdAndBusinessId(UUID id, UUID businessId);
    Page<JobType> findAllByBusinessId(UUID businessId, Pageable pageable);
    boolean existsByBusinessIdAndCodeIgnoreCase(UUID businessId, String code);
}

interface ChecklistTemplateRepository extends JpaRepository<ChecklistTemplate, UUID> {
    Optional<ChecklistTemplate> findByIdAndBusinessId(UUID id, UUID businessId);
    Page<ChecklistTemplate> findAllByBusinessId(UUID businessId, Pageable pageable);
    boolean existsByBusinessIdAndCodeIgnoreCaseAndTemplateVersion(
            UUID businessId, String code, Integer templateVersion);
}

interface MaintenanceTemplateRepository extends JpaRepository<MaintenanceTemplate, UUID> {
    Optional<MaintenanceTemplate> findByIdAndBusinessId(UUID id, UUID businessId);
    Page<MaintenanceTemplate> findAllByBusinessId(UUID businessId, Pageable pageable);
    boolean existsByBusinessIdAndCodeIgnoreCaseAndTemplateVersion(
            UUID businessId, String code, Integer templateVersion);
}

interface UnitOfMeasureRepository extends JpaRepository<UnitOfMeasure, UUID> {
    Optional<UnitOfMeasure> findByIdAndBusinessId(UUID id, UUID businessId);
    Page<UnitOfMeasure> findAllByBusinessId(UUID businessId, Pageable pageable);
    boolean existsByBusinessIdAndCodeIgnoreCase(UUID businessId, String code);
}
