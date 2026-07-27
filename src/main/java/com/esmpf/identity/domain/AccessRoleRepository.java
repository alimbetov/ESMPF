package com.esmpf.identity.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface AccessRoleRepository extends JpaRepository<AccessRole, UUID> {

    Optional<AccessRole> findByBusinessIdAndCodeIgnoreCase(UUID businessId, String code);
}
