package com.esmpf.identity.auth;

import java.util.Optional;
import java.util.UUID;

/**
 * Minimal persistence boundary needed by authentication services.
 * The identity module will implement this adapter using the existing UserAccount model.
 */
public interface AuthenticationUserGateway {

    Optional<AuthenticationUser> findByGoogleSubject(String googleSubject);

    Optional<AuthenticationUser> findByNormalizedEmail(String normalizedEmail);

    AuthenticationUser linkGoogleIdentity(UUID userId, String googleSubject);

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
            externalProvider = externalProvider == null ? null : externalProvider.trim();
            externalSubject = externalSubject == null ? null : externalSubject.trim();
        }

        public boolean hasGoogleIdentity() {
            return "GOOGLE".equalsIgnoreCase(externalProvider)
                    && externalSubject != null
                    && !externalSubject.isBlank();
        }
    }
}
