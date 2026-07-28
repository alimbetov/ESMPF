package com.esmpf.shared.security;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Trusted runtime principal resolved by the server after JWT validation.
 * The token itself carries only userId; roles and permissions are loaded server-side.
 */
public record AuthenticatedActor(
        UUID userId,
        Set<String> roles,
        Set<String> permissions
) {

    public AuthenticatedActor {
        Objects.requireNonNull(userId, "userId is required");
        roles = normalizeAuthorities(roles);
        permissions = normalizeAuthorities(permissions);
    }

    public boolean hasRole(String role) {
        return roles.contains(normalizeAuthority(role));
    }

    public boolean hasPermission(String permission) {
        return permissions.contains(normalizeAuthority(permission));
    }

    private static Set<String> normalizeAuthorities(Collection<String> values) {
        if (values == null || values.isEmpty()) {
            return Set.of();
        }
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String value : values) {
            normalized.add(normalizeAuthority(value));
        }
        return Set.copyOf(normalized);
    }

    private static String normalizeAuthority(String value) {
        String normalized = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        if (!normalized.matches("[A-Z][A-Z0-9_]{1,99}")) {
            throw new IllegalArgumentException(
                    "role and permission codes must contain 2-100 uppercase latin letters, digits or underscores");
        }
        return normalized;
    }
}
