package com.esmpf.web;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("esmpf.security.jwt")
public record JwtSecurityProperties(
        boolean enabled,
        String issuer,
        String audience,
        String secret,
        Duration accessTokenTtl,
        Duration clockSkew
) {
    public JwtSecurityProperties {
        issuer = issuer == null || issuer.isBlank() ? "esmpf" : issuer.trim();
        audience = audience == null || audience.isBlank() ? "esmpf-api" : audience.trim();
        secret = secret == null ? "" : secret;
        accessTokenTtl = accessTokenTtl == null ? Duration.ofMinutes(15) : accessTokenTtl;
        clockSkew = clockSkew == null ? Duration.ofSeconds(30) : clockSkew;
        if (enabled) {
            if (secret.getBytes(java.nio.charset.StandardCharsets.UTF_8).length < 32) {
                throw new IllegalStateException("ESMPF JWT secret must contain at least 32 UTF-8 bytes");
            }
            if (accessTokenTtl.isZero() || accessTokenTtl.isNegative() || accessTokenTtl.compareTo(Duration.ofHours(1)) > 0) {
                throw new IllegalStateException("ESMPF access-token TTL must be between 1 second and 1 hour");
            }
            if (clockSkew.isNegative() || clockSkew.compareTo(Duration.ofMinutes(5)) > 0) {
                throw new IllegalStateException("ESMPF JWT clock skew must be between zero and five minutes");
            }
        }
    }
}
