package com.esmpf.shared.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

/**
 * Minimal stateless HMAC access-token utility.
 * The token identifies only the user. Business, roles and permissions are resolved server-side.
 */
public final class JwtUtility {

    public static final String TOKEN_TYPE = "token_type";
    public static final String TOKEN_VERSION = "ver";
    public static final String ACCESS_TOKEN = "access";
    public static final int CURRENT_VERSION = 1;

    private final byte[] secret;
    private final String issuer;
    private final String audience;
    private final Duration accessTokenTtl;
    private final Duration clockSkew;
    private final Clock clock;

    public JwtUtility(String secret, String issuer, String audience,
                      Duration accessTokenTtl, Duration clockSkew, Clock clock) {
        this.secret = validateSecret(secret);
        this.issuer = requireText(issuer, "issuer");
        this.audience = requireText(audience, "audience");
        this.accessTokenTtl = requirePositive(accessTokenTtl, "accessTokenTtl");
        this.clockSkew = requireNonNegative(clockSkew, "clockSkew");
        this.clock = Objects.requireNonNull(clock, "clock is required");
    }

    public String issueAccessToken(UUID userId) {
        Objects.requireNonNull(userId, "userId is required");
        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plus(accessTokenTtl);

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .audience(audience)
                .subject(userId.toString())
                .jwtID(UUID.randomUUID().toString())
                .issueTime(Date.from(issuedAt))
                .notBeforeTime(Date.from(issuedAt))
                .expirationTime(Date.from(expiresAt))
                .claim(TOKEN_TYPE, ACCESS_TOKEN)
                .claim(TOKEN_VERSION, CURRENT_VERSION)
                .build();

        SignedJWT jwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.HS256)
                        .type(com.nimbusds.jose.JOSEObjectType.JWT)
                        .build(),
                claims);
        try {
            jwt.sign(new MACSigner(secret));
            return jwt.serialize();
        } catch (JOSEException exception) {
            throw new SecurityAccessException("JWT signing failed", exception);
        }
    }

    public ValidatedJwt validateAccessToken(String serializedToken) {
        String token = requireText(serializedToken, "token");
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            if (!JWSAlgorithm.HS256.equals(jwt.getHeader().getAlgorithm())) {
                throw invalid("unexpected JWT algorithm");
            }
            if (!jwt.verify(new MACVerifier(secret))) {
                throw invalid("invalid JWT signature");
            }

            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            validateRegisteredClaims(claims);
            UUID userId = parseUuid(claims.getSubject(), "sub");

            return new ValidatedJwt(
                    claims.getJWTID(),
                    claims.getIssueTime().toInstant(),
                    claims.getExpirationTime().toInstant(),
                    userId);
        } catch (ParseException | JOSEException | IllegalArgumentException exception) {
            if (exception instanceof SecurityAccessException accessException) {
                throw accessException;
            }
            throw invalid("invalid JWT", exception);
        }
    }

    private void validateRegisteredClaims(JWTClaimsSet claims) throws ParseException {
        Instant now = clock.instant();
        if (!issuer.equals(claims.getIssuer())) {
            throw invalid("invalid JWT issuer");
        }
        if (claims.getAudience() == null || !claims.getAudience().contains(audience)) {
            throw invalid("invalid JWT audience");
        }
        if (!ACCESS_TOKEN.equals(claims.getStringClaim(TOKEN_TYPE))) {
            throw invalid("invalid token type");
        }
        Number version = claims.getIntegerClaim(TOKEN_VERSION);
        if (version == null || version.intValue() != CURRENT_VERSION) {
            throw invalid("unsupported token version");
        }
        if (claims.getJWTID() == null || claims.getJWTID().isBlank()) {
            throw invalid("jti is required");
        }
        Date issuedAt = claims.getIssueTime();
        Date notBefore = claims.getNotBeforeTime();
        Date expiresAt = claims.getExpirationTime();
        if (issuedAt == null || notBefore == null || expiresAt == null) {
            throw invalid("iat, nbf and exp are required");
        }
        if (issuedAt.toInstant().isAfter(now.plus(clockSkew))) {
            throw invalid("token issued in the future");
        }
        if (notBefore.toInstant().isAfter(now.plus(clockSkew))) {
            throw invalid("token is not active yet");
        }
        if (!expiresAt.toInstant().isAfter(now.minus(clockSkew))) {
            throw invalid("token has expired");
        }
    }

    private static UUID parseUuid(String value, String claim) {
        try {
            return UUID.fromString(requireText(value, claim));
        } catch (IllegalArgumentException exception) {
            throw invalid("invalid UUID claim: " + claim, exception);
        }
    }

    private static byte[] validateSecret(String secret) {
        byte[] bytes = requireText(secret, "secret").getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalArgumentException("JWT secret must contain at least 32 UTF-8 bytes");
        }
        return bytes.clone();
    }

    private static String requireText(String value, String name) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(name + " is required");
        }
        return normalized;
    }

    private static Duration requirePositive(Duration value, String name) {
        Objects.requireNonNull(value, name + " is required");
        if (value.isZero() || value.isNegative()) {
            throw new IllegalArgumentException(name + " must be positive");
        }
        return value;
    }

    private static Duration requireNonNegative(Duration value, String name) {
        Objects.requireNonNull(value, name + " is required");
        if (value.isNegative()) {
            throw new IllegalArgumentException(name + " must not be negative");
        }
        return value;
    }

    private static SecurityAccessException invalid(String message) {
        return new SecurityAccessException(message);
    }

    private static SecurityAccessException invalid(String message, Throwable cause) {
        return new SecurityAccessException(message, cause);
    }

    public record ValidatedJwt(
            String tokenId,
            Instant issuedAt,
            Instant expiresAt,
            UUID userId
    ) {
    }
}
