package com.esmpf.web;

import com.esmpf.shared.security.EsmpfPrincipal;
import com.esmpf.shared.security.JwtUtility;
import com.esmpf.shared.security.PersistedAccessResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

final class BearerTokenAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtility jwtUtility;
    private final PersistedAccessResolver accessResolver;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final BearerTokenParser tokenParser = new BearerTokenParser();

    BearerTokenAuthenticationFilter(
            JwtUtility jwtUtility,
            PersistedAccessResolver accessResolver,
            AuthenticationEntryPoint authenticationEntryPoint
    ) {
        this.jwtUtility = jwtUtility;
        this.accessResolver = accessResolver;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String token = tokenParser.parse(request);
            Authentication existing = SecurityContextHolder.getContext().getAuthentication();

            if (token == null) {
                if (existing == null) {
                    filterChain.doFilter(request, response);
                    return;
                }
                if (existing.isAuthenticated() && existing.getPrincipal() instanceof EsmpfPrincipal) {
                    filterChain.doFilter(request, response);
                    return;
                }
                SecurityContextHolder.clearContext();
                reject(request, response, new BadCredentialsException("Untrusted pre-authentication principal"));
                return;
            }

            SecurityContextHolder.clearContext();
            var validated = jwtUtility.validateAccessToken(token);
            var access = accessResolver.resolve(validated.userId());
            Set<String> roleCodes = access.roleCodes();
            Set<String> permissions = access.permissionCodes();
            EsmpfPrincipal principal = new EsmpfPrincipal(
                    access.userId(), access.businessId(), roleCodes, permissions);
            List<SimpleGrantedAuthority> authorities = java.util.stream.Stream.concat(
                            permissions.stream().map(SimpleGrantedAuthority::new),
                            roleCodes.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)))
                    .toList();
            var authentication = UsernamePasswordAuthenticationToken.authenticated(
                    principal, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (RuntimeException exception) {
            SecurityContextHolder.clearContext();
            reject(request, response, new BadCredentialsException("Invalid access token", exception));
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/v1/public/") || "/api/v1/auth/google".equals(path);
    }

    private void reject(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {
        authenticationEntryPoint.commence(request, response, exception);
    }
}
