package com.esmpf.shared.security;

import java.util.Arrays;

/**
 * Application-layer authorization checks reusable by services and future method security.
 */
public final class AuthorizationGuard {

    private AuthorizationGuard() {
    }

    public static void requireRole(AuthenticatedActor actor, String role) {
        requireActor(actor);
        if (!actor.hasRole(role)) {
            throw new SecurityAccessException("required role is missing: " + role);
        }
    }

    public static void requirePermission(AuthenticatedActor actor, String permission) {
        requireActor(actor);
        if (!actor.hasPermission(permission)) {
            throw new SecurityAccessException("required permission is missing: " + permission);
        }
    }

    public static void requireAnyPermission(AuthenticatedActor actor, String... permissions) {
        requireActor(actor);
        if (permissions == null || permissions.length == 0) {
            throw new IllegalArgumentException("at least one permission is required");
        }
        if (Arrays.stream(permissions).noneMatch(actor::hasPermission)) {
            throw new SecurityAccessException("none of the required permissions is present");
        }
    }

    private static void requireActor(AuthenticatedActor actor) {
        if (actor == null) {
            throw new SecurityAccessException("authenticated actor is required");
        }
    }
}
