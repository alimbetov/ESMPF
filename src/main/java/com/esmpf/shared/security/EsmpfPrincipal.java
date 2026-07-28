package com.esmpf.shared.security;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Immutable server-resolved principal. Only userId comes from the validated JWT;
 * business, roles and permissions are loaded from persisted ESMPF state.
 */
public record EsmpfPrincipal(
        UUID userId,
        UUID businessId,
        Set<String> roles,
        Set<String> permissions
) {
    public EsmpfPrincipal {
        Objects.requireNonNull(userId, "userId is required");
        Objects.requireNonNull(businessId, "businessId is required");
        AuthenticatedActor normalized = new AuthenticatedActor(userId, roles, permissions);
        roles = normalized.roles();
        permissions = normalized.permissions();
    }

    public AuthenticatedActor actor() {
        return new AuthenticatedActor(userId, roles, permissions);
    }

    public boolean hasPermission(String permission) {
        return actor().hasPermission(permission);
    }
}
