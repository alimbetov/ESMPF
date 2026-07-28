package com.esmpf.shared.security;

import java.util.Set;
import java.util.UUID;

/** Security-runtime port implemented by the identity module. */
public interface PersistedAccessResolver {
    ResolvedAccess resolve(UUID userId);

    record ResolvedAccess(
            UUID userId,
            UUID businessId,
            Set<String> roleCodes,
            Set<String> permissionCodes
    ) {}
}
