package com.esmpf.identity;

import static com.esmpf.identity.RbacDtos.EffectiveAccess;

import java.util.UUID;

public interface AccessControlQuery {
    EffectiveAccess resolveEffectiveAccess(UUID userId);
    boolean hasPermission(UUID userId, PermissionCode permission);
}
