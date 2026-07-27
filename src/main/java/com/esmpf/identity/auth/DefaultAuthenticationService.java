package com.esmpf.identity.auth;

import static com.esmpf.identity.auth.AuthenticationDtos.AuthenticationResponse;
import static com.esmpf.identity.auth.AuthenticationDtos.GoogleSignInCommand;

import com.esmpf.identity.auth.AuthenticationUserGateway.AuthenticationUser;
import com.esmpf.identity.auth.GoogleIdentityVerifier.GooglePrincipal;
import com.esmpf.shared.security.JwtUtility;
import com.esmpf.shared.security.SecurityAccessException;
import java.util.Locale;
import java.util.Objects;

/**
 * Simple pre-provisioned Google sign-in flow.
 *
 * Unknown Google accounts are not auto-created. On the first successful sign-in, an active
 * ESMPF user with the same verified normalized email is linked to the Google subject.
 */
public final class DefaultAuthenticationService implements AuthenticationService {

    private final GoogleIdentityVerifier googleIdentityVerifier;
    private final AuthenticationUserGateway userGateway;
    private final JwtUtility jwtUtility;

    public DefaultAuthenticationService(
            GoogleIdentityVerifier googleIdentityVerifier,
            AuthenticationUserGateway userGateway,
            JwtUtility jwtUtility
    ) {
        this.googleIdentityVerifier = Objects.requireNonNull(
                googleIdentityVerifier, "googleIdentityVerifier is required");
        this.userGateway = Objects.requireNonNull(userGateway, "userGateway is required");
        this.jwtUtility = Objects.requireNonNull(jwtUtility, "jwtUtility is required");
    }

    @Override
    public AuthenticationResponse signInWithGoogle(GoogleSignInCommand command) {
        Objects.requireNonNull(command, "command is required");

        GooglePrincipal google = googleIdentityVerifier.verify(command.credential());
        if (!google.emailVerified()) {
            throw new SecurityAccessException("Google email is not verified");
        }

        AuthenticationUser user = userGateway.findByGoogleSubject(google.subject())
                .orElseGet(() -> linkPreProvisionedUser(google));

        requireActive(user);
        String accessToken = jwtUtility.issueAccessToken(user.id());
        var validated = jwtUtility.validateAccessToken(accessToken);

        return new AuthenticationResponse(
                accessToken,
                "Bearer",
                validated.expiresAt(),
                user.id(),
                user.email(),
                user.fullName());
    }

    private AuthenticationUser linkPreProvisionedUser(GooglePrincipal google) {
        String normalizedEmail = google.email().trim().toLowerCase(Locale.ROOT);
        AuthenticationUser user = userGateway.findByNormalizedEmail(normalizedEmail)
                .orElseThrow(() -> new SecurityAccessException("Google account is not provisioned in ESMPF"));

        requireActive(user);
        if (user.hasGoogleIdentity() && !google.subject().equals(user.externalSubject())) {
            throw new SecurityAccessException("ESMPF user is already linked to another Google account");
        }
        if (user.hasGoogleIdentity()) {
            return user;
        }
        return userGateway.linkGoogleIdentity(user.id(), google.subject());
    }

    private static void requireActive(AuthenticationUser user) {
        if (!user.active()) {
            throw new SecurityAccessException("ESMPF user account is inactive");
        }
    }
}
