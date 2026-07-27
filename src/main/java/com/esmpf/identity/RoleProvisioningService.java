package com.esmpf.identity;

import java.util.UUID;

public interface RoleProvisioningService {

    AccessRoleReference ensureRole(
            UUID businessId,
            String code,
            String name,
            String description,
            boolean system
    );

    record AccessRoleReference(
            UUID id,
            UUID businessId,
            String code,
            String name,
            String description,
            boolean system,
            boolean active,
            long version
    ) {
    }
}
