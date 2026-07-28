package com.esmpf.internationalization.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessInternationalProfileRepository extends JpaRepository<BusinessInternationalProfile, UUID> {

    Optional<BusinessInternationalProfile> findByBusinessId(UUID businessId);
}
