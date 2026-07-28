package com.esmpf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.esmpf.identity.auth.AuthenticationDtos.GoogleSignInCommand;
import com.esmpf.identity.auth.AuthenticationUserGateway;
import com.esmpf.identity.auth.AuthenticationUserGateway.AuthenticationUser;
import com.esmpf.identity.auth.DefaultAuthenticationService;
import com.esmpf.identity.auth.GoogleIdentityVerifier;
import com.esmpf.identity.auth.GoogleIdentityVerifier.GooglePrincipal;
import com.esmpf.shared.security.JwtUtility;
import com.esmpf.shared.security.SecurityAccessException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GoogleAuthenticationServiceTests {

    private static final Instant NOW = Instant.parse("2026-07-27T12:00:00Z");
    private static final String SECRET =
            "test-only-secret-with-at-least-thirty-two-bytes-123456789";

    @Test
    void linksPreProvisionedActiveUserAndIssuesEsmpfToken() {
        UUID userId = UUID.randomUUID();
        InMemoryGateway gateway = new InMemoryGateway();
        gateway.add(new AuthenticationUser(
                userId, "user@example.com", "Test User", true, null, null));

        DefaultAuthenticationService service = service(
                new GooglePrincipal("google-sub-1", "USER@example.com", true, "Test User", null),
                gateway);

        var response = service.signInWithGoogle(new GoogleSignInCommand("google-credential"));

        assertEquals(userId, response.userId());
        assertEquals("Bearer", response.tokenType());
        assertEquals("user@example.com", response.email());
        assertEquals(userId, jwt().validateAccessToken(response.accessToken()).userId());
        assertTrue(gateway.findByGoogleSubject("google-sub-1").isPresent());
    }

    @Test
    void returnsExistingGoogleLinkedUserWithoutRelinking() {
        UUID userId = UUID.randomUUID();
        InMemoryGateway gateway = new InMemoryGateway();
        gateway.add(new AuthenticationUser(
                userId, "user@example.com", "Test User", true, "GOOGLE", "google-sub-1"));

        var response = service(
                new GooglePrincipal("google-sub-1", "other@example.com", true, "Changed Name", null),
                gateway).signInWithGoogle(new GoogleSignInCommand("credential"));

        assertEquals(userId, response.userId());
        assertEquals(0, gateway.linkCalls);
    }

    @Test
    void rejectsUnknownInactiveAndUnverifiedAccounts() {
        InMemoryGateway unknownGateway = new InMemoryGateway();
        assertThrows(SecurityAccessException.class, () -> service(
                new GooglePrincipal("sub", "unknown@example.com", true, null, null),
                unknownGateway).signInWithGoogle(new GoogleSignInCommand("credential")));

        InMemoryGateway inactiveGateway = new InMemoryGateway();
        inactiveGateway.add(new AuthenticationUser(
                UUID.randomUUID(), "inactive@example.com", "Inactive", false, null, null));
        assertThrows(SecurityAccessException.class, () -> service(
                new GooglePrincipal("sub", "inactive@example.com", true, null, null),
                inactiveGateway).signInWithGoogle(new GoogleSignInCommand("credential")));

        InMemoryGateway unverifiedGateway = new InMemoryGateway();
        unverifiedGateway.add(new AuthenticationUser(
                UUID.randomUUID(), "user@example.com", "User", true, null, null));
        assertThrows(SecurityAccessException.class, () -> service(
                new GooglePrincipal("sub", "user@example.com", false, null, null),
                unverifiedGateway).signInWithGoogle(new GoogleSignInCommand("credential")));
    }

    @Test
    void rejectsRelinkingUserToDifferentGoogleSubject() {
        InMemoryGateway gateway = new InMemoryGateway();
        gateway.add(new AuthenticationUser(
                UUID.randomUUID(), "user@example.com", "User", true, "GOOGLE", "old-sub"));

        assertThrows(SecurityAccessException.class, () -> service(
                new GooglePrincipal("new-sub", "user@example.com", true, null, null),
                gateway).signInWithGoogle(new GoogleSignInCommand("credential")));
    }

    @Test
    void rejectsOverwritingAnotherExternalProvider() {
        InMemoryGateway gateway = new InMemoryGateway();
        gateway.add(new AuthenticationUser(
                UUID.randomUUID(), "user@example.com", "User", true, "MICROSOFT", "ms-sub"));

        assertThrows(SecurityAccessException.class, () -> service(
                new GooglePrincipal("google-sub", "user@example.com", true, null, null),
                gateway).signInWithGoogle(new GoogleSignInCommand("credential")));
        assertEquals(0, gateway.linkCalls);
    }

    private static DefaultAuthenticationService service(
            GooglePrincipal principal,
            InMemoryGateway gateway
    ) {
        GoogleIdentityVerifier verifier = credential -> principal;
        return new DefaultAuthenticationService(verifier, gateway, jwt());
    }

    private static JwtUtility jwt() {
        return new JwtUtility(
                SECRET,
                "esmpf",
                "esmpf-api",
                Duration.ofMinutes(15),
                Duration.ZERO,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private static final class InMemoryGateway implements AuthenticationUserGateway {
        private final Map<UUID, AuthenticationUser> users = new HashMap<>();
        private int linkCalls;

        void add(AuthenticationUser user) {
            users.put(user.id(), user);
        }

        @Override
        public Optional<AuthenticationUser> findByGoogleSubject(String googleSubject) {
            return users.values().stream()
                    .filter(AuthenticationUser::hasGoogleIdentity)
                    .filter(user -> googleSubject.equals(user.externalSubject()))
                    .findFirst();
        }

        @Override
        public Optional<AuthenticationUser> findByNormalizedEmail(String normalizedEmail) {
            String email = normalizedEmail.toLowerCase(Locale.ROOT);
            return users.values().stream()
                    .filter(user -> email.equals(user.email()))
                    .findFirst();
        }

        @Override
        public AuthenticationUser linkGoogleIdentityIfAbsent(UUID userId, String googleSubject) {
            linkCalls++;
            AuthenticationUser current = users.get(userId);
            if (current.hasExternalIdentity() && !current.isGoogleIdentity(googleSubject)) {
                throw new IllegalStateException("external identity already linked");
            }
            AuthenticationUser linked = new AuthenticationUser(
                    current.id(), current.email(), current.fullName(), current.active(),
                    "GOOGLE", googleSubject);
            users.put(userId, linked);
            return linked;
        }
    }
}
