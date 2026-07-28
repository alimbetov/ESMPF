package com.esmpf.web;

import com.esmpf.identity.AccessControlQuery;
import com.esmpf.shared.security.JwtUtility;
import java.time.Clock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.AuthenticationEntryPoint;

@Configuration
@EnableConfigurationProperties(JwtSecurityProperties.class)
class JwtRuntimeConfiguration {

    @Bean
    Clock securityClock() {
        return Clock.systemUTC();
    }

    @Bean
    @ConditionalOnProperty(prefix = "esmpf.security.jwt", name = "enabled", havingValue = "true")
    JwtUtility jwtUtility(JwtSecurityProperties properties, Clock securityClock) {
        return new JwtUtility(
                properties.secret(),
                properties.issuer(),
                properties.audience(),
                properties.accessTokenTtl(),
                properties.clockSkew(),
                securityClock);
    }

    @Bean
    @ConditionalOnProperty(prefix = "esmpf.security.jwt", name = "enabled", havingValue = "true")
    BearerTokenAuthenticationFilter bearerTokenAuthenticationFilter(
            JwtUtility jwtUtility,
            AccessControlQuery accessControlQuery,
            AuthenticationEntryPoint authenticationEntryPoint
    ) {
        return new BearerTokenAuthenticationFilter(jwtUtility, accessControlQuery, authenticationEntryPoint);
    }
}
