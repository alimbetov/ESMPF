package com.esmpf.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.esmpf.shared.security.EsmpfPrincipal;
import com.esmpf.shared.security.JwtUtility;
import com.esmpf.shared.security.PersistedAccessResolver;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

class BearerTokenAuthenticationFilterTests {
    private static final String SECRET = "test-only-secret-with-at-least-thirty-two-bytes-123456789";
    private static final Instant NOW = Instant.parse("2026-07-28T10:00:00Z");

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void buildsPrincipalFromPersistedAccessAndNotTokenAuthorities() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID businessId = UUID.randomUUID();
        JwtUtility utility = utility();
        PersistedAccessResolver resolver = requestedUserId -> {
            assertEquals(userId, requestedUserId);
            return new PersistedAccessResolver.ResolvedAccess(
                    userId, businessId, Set.of("VIEWER"), Set.of("CUSTOMER_READ"));
        };
        var filter = new BearerTokenAuthenticationFilter(
                utility, resolver, (request, response, exception) -> response.setStatus(401));
        var request = new MockHttpServletRequest("GET", "/api/v1/customers");
        request.addHeader("Authorization", "Bearer " + utility.issueAccessToken(userId));
        var response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, new MockFilterChain());

        EsmpfPrincipal principal = assertInstanceOf(
                EsmpfPrincipal.class,
                SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        assertEquals(userId, principal.userId());
        assertEquals(businessId, principal.businessId());
        assertTrue(principal.permissions().contains("CUSTOMER_READ"));
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_VIEWER")));
    }

    @Test
    void rejectsInvalidBearerTokenWithoutResolvingPersistedAccess() throws Exception {
        PersistedAccessResolver resolver = userId -> {
            throw new AssertionError("resolver must not be called for an invalid JWT");
        };
        var filter = new BearerTokenAuthenticationFilter(
                utility(), resolver, (request, response, exception) -> response.setStatus(401));
        var request = new MockHttpServletRequest("GET", "/api/v1/customers");
        request.addHeader("Authorization", "Bearer invalid.token.value");
        var response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, new MockFilterChain());

        assertEquals(401, response.getStatus());
        assertTrue(SecurityContextHolder.getContext().getAuthentication() == null);
    }

    private static JwtUtility utility() {
        return new JwtUtility(
                SECRET, "esmpf", "esmpf-api", Duration.ofMinutes(15), Duration.ZERO,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }
}
