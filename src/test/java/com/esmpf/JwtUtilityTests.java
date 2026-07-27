package com.esmpf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.esmpf.shared.security.AuthenticatedActor;
import com.esmpf.shared.security.AuthorizationGuard;
import com.esmpf.shared.security.JwtUtility;
import com.esmpf.shared.security.SecurityAccessException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtUtilityTests {

    private static final String SECRET =
            "test-only-secret-with-at-least-thirty-two-bytes-123456789";
    private static final Instant NOW = Instant.parse("2026-07-27T12:00:00Z");

    @Test
    void issuesAndValidatesAccessTokenWithTenantRolesAndPermissions() {
        UUID userId = UUID.randomUUID();
        UUID businessId = UUID.randomUUID();
        AuthenticatedActor actor = new AuthenticatedActor(
                userId,
                businessId,
                Set.of("admin"),
                Set.of("customer_read", "customer_update"));

        JwtUtility utility = utilityAt(NOW);
        var validated = utility.validateAccessToken(utility.issueAccessToken(actor));

        assertEquals(userId, validated.actor().userId());
        assertEquals(businessId, validated.actor().businessId());
        assertEquals(Set.of("ADMIN"), validated.actor().roles());
        assertEquals(Set.of("CUSTOMER_READ", "CUSTOMER_UPDATE"),
                validated.actor().permissions());
        assertTrue(validated.expiresAt().isAfter(validated.issuedAt()));
    }

    @Test
    void rejectsTamperedTokenAndWrongVerifierSecret() {
        JwtUtility issuer = utilityAt(NOW);
        String token = issuer.issueAccessToken(actor());
        JwtUtility wrongVerifier = new JwtUtility(
                "another-test-secret-with-at-least-thirty-two-bytes-987654321",
                "esmpf",
                "esmpf-api",
                Duration.ofMinutes(15),
                Duration.ofSeconds(30),
                Clock.fixed(NOW, ZoneOffset.UTC));

        assertThrows(SecurityAccessException.class,
                () -> wrongVerifier.validateAccessToken(token));
        assertThrows(SecurityAccessException.class,
                () -> issuer.validateAccessToken(token.substring(0, token.length() - 2) + "aa"));
    }

    @Test
    void rejectsExpiredTokenAndWrongAudience() {
        String token = utilityAt(NOW).issueAccessToken(actor());
        JwtUtility expiredVerifier = utilityAt(NOW.plus(Duration.ofMinutes(16)));
        JwtUtility wrongAudience = new JwtUtility(
                SECRET,
                "esmpf",
                "another-api",
                Duration.ofMinutes(15),
                Duration.ZERO,
                Clock.fixed(NOW, ZoneOffset.UTC));

        assertThrows(SecurityAccessException.class,
                () -> expiredVerifier.validateAccessToken(token));
        assertThrows(SecurityAccessException.class,
                () -> wrongAudience.validateAccessToken(token));
    }

    @Test
    void rejectsWeakSecret() {
        assertThrows(IllegalArgumentException.class, () -> new JwtUtility(
                "weak-secret",
                "esmpf",
                "esmpf-api",
                Duration.ofMinutes(15),
                Duration.ZERO,
                Clock.systemUTC()));
    }

    @Test
    void authorizationGuardPreventsCrossTenantAndMissingAuthority() {
        AuthenticatedActor actor = actor();

        AuthorizationGuard.requireBusiness(actor, actor.businessId());
        AuthorizationGuard.requireRole(actor, "admin");
        AuthorizationGuard.requirePermission(actor, "customer_read");
        AuthorizationGuard.requireAnyPermission(actor, "CUSTOMER_UPDATE", "CUSTOMER_READ");

        assertThrows(SecurityAccessException.class,
                () -> AuthorizationGuard.requireBusiness(actor, UUID.randomUUID()));
        assertThrows(SecurityAccessException.class,
                () -> AuthorizationGuard.requireRole(actor, "OWNER"));
        assertThrows(SecurityAccessException.class,
                () -> AuthorizationGuard.requirePermission(actor, "CUSTOMER_DELETE"));
    }

    private static JwtUtility utilityAt(Instant instant) {
        return new JwtUtility(
                SECRET,
                "esmpf",
                "esmpf-api",
                Duration.ofMinutes(15),
                Duration.ZERO,
                Clock.fixed(instant, ZoneOffset.UTC));
    }

    private static AuthenticatedActor actor() {
        return new AuthenticatedActor(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Set.of("ADMIN"),
                Set.of("CUSTOMER_READ"));
    }
}
