package com.esmpf.identity.domain;

import com.esmpf.identity.RoleProvisioningService;
import java.util.Locale;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Service
class RoleProvisioningServiceImpl implements RoleProvisioningService {

    private final AccessRoleRepository repository;
    private final TransactionTemplate requiresNewTransaction;

    RoleProvisioningServiceImpl(
            AccessRoleRepository repository,
            PlatformTransactionManager transactionManager
    ) {
        this.repository = repository;
        this.requiresNewTransaction = new TransactionTemplate(transactionManager);
        this.requiresNewTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public AccessRoleReference ensureRole(
            UUID businessId,
            String code,
            String name,
            String description,
            boolean system
    ) {
        UUID tenant = requireBusinessId(businessId);
        String normalizedCode = normalizeCode(code);

        return repository.findByBusinessIdAndCodeIgnoreCase(tenant, normalizedCode)
                .map(this::toReference)
                .orElseGet(() -> createOrReadConcurrent(
                        tenant,
                        normalizedCode,
                        normalizeName(name),
                        trimToNull(description),
                        system));
    }

    private AccessRoleReference createOrReadConcurrent(
            UUID businessId,
            String code,
            String name,
            String description,
            boolean system
    ) {
        try {
            AccessRoleReference created = requiresNewTransaction.execute(status -> {
                AccessRole role = new AccessRole();
                role.setBusinessId(businessId);
                role.setCode(code);
                role.setName(name);
                role.setDescription(description);
                role.setSystem(system);
                role.setActive(true);
                return toReference(repository.saveAndFlush(role));
            });
            if (created == null) {
                throw new IllegalStateException("Role provisioning transaction returned no result");
            }
            return created;
        } catch (DataIntegrityViolationException exception) {
            return repository.findByBusinessIdAndCodeIgnoreCase(businessId, code)
                    .map(this::toReference)
                    .orElseThrow(() -> exception);
        }
    }

    private AccessRoleReference toReference(AccessRole role) {
        return new AccessRoleReference(
                role.getId(),
                role.getBusinessId(),
                role.getCode(),
                role.getName(),
                role.getDescription(),
                role.isSystem(),
                role.isActive(),
                role.getVersion());
    }

    private static UUID requireBusinessId(UUID businessId) {
        if (businessId == null) {
            throw new IllegalArgumentException("businessId is required for role provisioning");
        }
        return businessId;
    }

    private static String normalizeCode(String value) {
        String normalized = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        if (!normalized.matches("[A-Z][A-Z0-9_]{1,79}")) {
            throw new IllegalArgumentException(
                    "role code must contain 2-80 uppercase latin letters, digits or underscores");
        }
        return normalized;
    }

    private static String normalizeName(String value) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty() || normalized.length() > 150) {
            throw new IllegalArgumentException("role name must contain 1-150 characters");
        }
        return normalized;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > 500) {
            throw new IllegalArgumentException("role description must not exceed 500 characters");
        }
        return normalized.isEmpty() ? null : normalized;
    }
}
