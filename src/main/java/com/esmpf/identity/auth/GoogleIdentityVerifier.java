package com.esmpf.identity.auth;

/**
 * Boundary around Google ID-token verification.
 *
 * The adapter must verify signature, audience, issuer and expiration before returning
 * a principal. Controllers and application services must never decode Google JWTs directly.
 */
public interface GoogleIdentityVerifier {

    GooglePrincipal verify(String credential);

    record GooglePrincipal(
            String subject,
            String email,
            boolean emailVerified,
            String fullName,
            String pictureUrl
    ) {
        public GooglePrincipal {
            subject = requireText(subject, "Google subject");
            email = requireText(email, "Google email").toLowerCase(java.util.Locale.ROOT);
            fullName = fullName == null ? null : fullName.trim();
            pictureUrl = pictureUrl == null ? null : pictureUrl.trim();
        }

        private static String requireText(String value, String name) {
            String normalized = value == null ? "" : value.trim();
            if (normalized.isEmpty()) {
                throw new IllegalArgumentException(name + " is required");
            }
            return normalized;
        }
    }
}
