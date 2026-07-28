package com.esmpf.identity.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface AccessRoleRepository extends JpaRepository<AccessRole, UUID> {
    Optional<AccessRole> findByIdAndBusinessId(UUID id, UUID businessId);
    Optional<AccessRole> findByBusinessIdAndCodeIgnoreCase(UUID businessId, String code);
    Page<AccessRole> findAllByBusinessId(UUID businessId, Pageable pageable);
}
