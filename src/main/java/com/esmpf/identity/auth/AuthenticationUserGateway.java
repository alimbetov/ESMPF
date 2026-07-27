package com.esmpf.identity.auth;

import java.util.Optional;
import java.util.UUID;

/**
 * Minimal persistence boundary needed by authentication services.
 *
 * The adapter must enforce a unique constraint for provider plus external subject and perform
 * {@link #linkGoogleIdentityIfAbsent(UUID, String)} atomically. Concurrent or conflicting links
 * must fail instead of overwriting an existing external identity.
 */
public interface AuthenticationUserGateway {

    Optional<AuthenticationUser> findByGoogleSubject(String googleSubject);

    Optional<AuthenticationUser> findByNormalizedEmail(String normalizedEmail);

    AuthenticationUser linkGoogleIdentityIfAbsent(UUID userId, String googleSubject);

    record AuthenticationUser(
            UUID id,
            String email,
            String fullName,
            boolean active,
            String externalProvider,
            String externalSubject
    ) {
        public AuthenticationUser {
            if (id == null) {
                throw new IllegalArgumentException("user id is required");
            }
            email = email == null ? null : email.trim().toLowerCase(java.util.Locale.ROOT);
            fullName = fullName == null ? null : fullName.trim();
            externalProvider = normalize(externalProvider);
            externalSubject = normalize(externalSubject);
        }

        public boolean hasExternalIdentity() {
            return externalProvider != null && externalSubject != null;
        }

        public boolean isGoogleIdentity(String googleSubject) {
            return "GOOGLE".equalsIgnoreCase(externalProvider)
                    && externalSubject != null
                    && externalSubject.equals(googleSubject);
        }

        public boolean hasGoogleIdentity() {
            return "GOOGLE".equalsIgnoreCase(externalProvider) && externalSubject != null;
        }

        private static String normalize(String value) {
            if (value == null) {
                return null;
            }
            String normalized = value.trim();
            return normalized.isEmpty() ? null : normalized;
        }
    }
}
