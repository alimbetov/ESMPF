package com.esmpf.customer.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByIdAndBusinessId(UUID id, UUID businessId);
    Page<Customer> findAllByBusinessId(UUID businessId, Pageable pageable);
}

interface ServiceLocationRepository extends JpaRepository<ServiceLocation, UUID> {
    Optional<ServiceLocation> findByIdAndBusinessId(UUID id, UUID businessId);
    Page<ServiceLocation> findAllByBusinessIdAndCustomerId(UUID businessId, UUID customerId, Pageable pageable);
    boolean existsByBusinessIdAndParentLocationId(UUID businessId, UUID parentLocationId);
}
