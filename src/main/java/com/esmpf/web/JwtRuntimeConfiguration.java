package com.esmpf.web;

import com.esmpf.shared.security.JwtUtility;
import com.esmpf.shared.security.PersistedAccessResolver;
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
            PersistedAccessResolver accessResolver,
            AuthenticationEntryPoint authenticationEntryPoint
    ) {
        return new BearerTokenAuthenticationFilter(jwtUtility, accessResolver, authenticationEntryPoint);
    }
}
