package com.esmpf.commercial.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface EstimateRepository extends JpaRepository<Estimate, UUID> {
    Optional<Estimate> findByIdAndBusinessId(UUID id, UUID businessId);
    Page<Estimate> findAllByBusinessId(UUID businessId, Pageable pageable);
    boolean existsByBusinessIdAndNumberIgnoreCase(UUID businessId, String number);
}

interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    Optional<Invoice> findByIdAndBusinessId(UUID id, UUID businessId);
    Page<Invoice> findAllByBusinessId(UUID businessId, Pageable pageable);
    boolean existsByBusinessIdAndNumberIgnoreCase(UUID businessId, String number);
    boolean existsByBusinessIdAndEstimateId(UUID businessId, UUID estimateId);
}

interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByIdAndBusinessId(UUID id, UUID businessId);
    Page<Payment> findAllByBusinessIdAndInvoiceId(UUID businessId, UUID invoiceId, Pageable pageable);
    boolean existsByBusinessIdAndExternalPaymentId(UUID businessId, String externalPaymentId);
}