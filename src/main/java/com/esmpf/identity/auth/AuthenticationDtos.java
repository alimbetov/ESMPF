package com.esmpf.identity.auth;

import java.time.Instant;
import java.util.UUID;

public final class AuthenticationDtos {

    private AuthenticationDtos() {
    }

    /**
     * Raw Google Identity Services credential. A future controller passes the received
     * credential value without decoding it.
     */
    public record GoogleSignInCommand(String credential) {
        public GoogleSignInCommand {
            if (credential == null || credential.isBlank()) {
                throw new IllegalArgumentException("Google credential is required");
            }
            credential = credential.trim();
        }
    }

    /**
     * Common authentication result suitable for password and Google sign-in controllers.
     */
    public record AuthenticationResponse(
            String accessToken,
            String tokenType,
            Instant expiresAt,
            UUID userId,
            String email,
            String fullName
    ) {
        public AuthenticationResponse {
            if (accessToken == null || accessToken.isBlank()) {
                throw new IllegalArgumentException("accessToken is required");
            }
            if (tokenType == null || tokenType.isBlank()) {
                throw new IllegalArgumentException("tokenType is required");
            }
            if (expiresAt == null) {
                throw new IllegalArgumentException("expiresAt is required");
            }
            if (userId == null) {
                throw new IllegalArgumentException("userId is required");
            }
        }
    }
}
