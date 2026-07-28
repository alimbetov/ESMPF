package com.esmpf.storage.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface StoredFileRepository extends JpaRepository<StoredFile, UUID> {
    Optional<StoredFile> findByIdAndBusinessId(UUID id, UUID businessId);
    Page<StoredFile> findAllByBusinessId(UUID businessId, Pageable pageable);
}
