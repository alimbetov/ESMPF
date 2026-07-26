package com.esmpf.customer.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface CustomerInteractionRepository extends JpaRepository<CustomerInteraction, UUID> {
    Optional<CustomerInteraction> findByIdAndBusinessId(UUID id, UUID businessId);
    Page<CustomerInteraction> findAllByBusinessIdAndCustomerId(UUID businessId, UUID customerId, Pageable pageable);
}