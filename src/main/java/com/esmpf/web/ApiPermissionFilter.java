package com.esmpf.web;

import com.esmpf.shared.security.EsmpfPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

final class ApiPermissionFilter extends OncePerRequestFilter {
    private final AccessDeniedHandler deniedHandler;
    private final List<Rule> rules = List.of(
        rule("GET", "/api/v1/access/permissions", "PERMISSION_READ"),
        rule("GET", "/api/v1/access/roles/**", "ROLE_READ"),
        rule("POST", "/api/v1/access/roles/*/actions/**", "ROLE_WRITE"),
        rule("PUT", "/api/v1/access/roles/**", "ROLE_WRITE"),
        rule("POST", "/api/v1/access/roles", "ROLE_WRITE"),
        rule("GET", "/api/v1/access/users/*/role-assignments", "ROLE_READ"),
        rule("POST", "/api/v1/access/users/*/role-assignments", "ROLE_ASSIGN"),
        rule("POST", "/api/v1/access/role-assignments/*/actions/revoke", "ROLE_ASSIGN"),
        rule("GET", "/api/v1/files/**", "FILE_READ"),
        rule("POST", "/api/v1/files", "FILE_UPLOAD"),
        rule("DELETE", "/api/v1/files/*", "FILE_DELETE"),
        rule("POST", "/api/v1/files/*/actions/restore", "FILE_RESTORE"),
        rw("/api/v1/customers/**", "CUSTOMER_READ", "CUSTOMER_WRITE"),
        rw("/api/v1/customer-interactions/**", "CUSTOMER_INTERACTION_READ", "CUSTOMER_INTERACTION_WRITE"),
        rw("/api/v1/equipment/**", "EQUIPMENT_READ", "EQUIPMENT_WRITE"),
        rw("/api/v1/service-requests/**", "SERVICE_REQUEST_READ", "SERVICE_REQUEST_WRITE"),
        rw("/api/v1/service-jobs/**", "SERVICE_JOB_READ", "SERVICE_JOB_WRITE"),
        rw("/api/v1/job-visits/**", "JOB_VISIT_READ", "JOB_VISIT_PLAN"),
        rw("/api/v1/job-executions/**", "WORK_EXECUTION_READ", "WORK_EXECUTION_EXECUTE"),
        rw("/api/v1/work-reports/**", "WORK_REPORT_READ", "WORK_REPORT_WRITE"),
        rw("/api/v1/materials/**", "MATERIAL_READ", "MATERIAL_WRITE"),
        rw("/api/v1/service-agreements/**", "SERVICE_AGREEMENT_READ", "SERVICE_AGREEMENT_WRITE"),
        rw("/api/v1/warranty-cases/**", "WARRANTY_READ", "WARRANTY_DECIDE"),
        any("/api/v1/mobile-devices/**", "DEVICE_SELF_MANAGE", "DEVICE_ADMIN"),
        any("/api/v1/users/*/mobile-devices", "DEVICE_SELF_MANAGE", "DEVICE_ADMIN"),
        rw("/api/v1/estimates/**", "ESTIMATE_READ", "ESTIMATE_WRITE"),
        rw("/api/v1/report-templates/**", "REPORT_TEMPLATE_READ", "REPORT_TEMPLATE_WRITE"),
        rw("/api/v1/documents/**", "DOCUMENT_READ", "DOCUMENT_GENERATE"),
        rw("/api/v1/attachments/**", "ATTACHMENT_READ", "ATTACHMENT_LINK"),
        rw("/api/v1/notifications/**", "NOTIFICATION_READ", "NOTIFICATION_SEND"),
        rw("/api/v1/feedback/**", "FEEDBACK_READ", "FEEDBACK_WRITE"),
        rw("/api/v1/content/**", "CONTENT_READ", "CONTENT_WRITE"),
        rw("/api/v1/business/**", "BUSINESS_READ", "BUSINESS_WRITE"),
        rw("/api/v1/locations/**", "LOCATION_READ", "LOCATION_WRITE"),
        rw("/api/v1/users/**", "USER_READ", "USER_WRITE"),
        rw("/api/v1/qualifications/**", "QUALIFICATION_READ", "QUALIFICATION_WRITE"),
        rw("/api/v1/catalog/**", "CATALOG_READ", "CATALOG_WRITE"),
        rw("/api/v1/maintenance/**", "MAINTENANCE_PLAN_READ", "MAINTENANCE_PLAN_WRITE"),
        rw("/api/v1/recommendations/**", "RECOMMENDATION_READ", "RECOMMENDATION_WRITE"),
        rw("/api/v1/integrations/**", "INTEGRATION_READ", "INTEGRATION_WRITE"),
        rule("GET", "/api/v1/audit/**", "AUDIT_READ")
    ).stream().flatMap(List::stream).toList();

    ApiPermissionFilter(AccessDeniedHandler deniedHandler) { this.deniedHandler = deniedHandler; }

    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!request.getRequestURI().startsWith("/api/v1/") || isPublic(request)) { chain.doFilter(request, response); return; }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken || !authentication.isAuthenticated()) {
            chain.doFilter(request, response);
            return;
        }
        if (!(authentication.getPrincipal() instanceof EsmpfPrincipal)) {
            deniedHandler.handle(request, response, new org.springframework.security.access.AccessDeniedException("Untrusted API principal"));
            return;
        }
        Rule matched = rules.stream().filter(r -> r.matcher.matches(request)).findFirst().orElse(null);
        if (matched == null || !hasAny(authentication, matched.permissions)) {
            deniedHandler.handle(request, response, new org.springframework.security.access.AccessDeniedException("Missing API permission"));
            return;
        }
        chain.doFilter(request, response);
    }

    private static boolean hasAny(Authentication authentication, Set<String> required) {
        Set<String> actual = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(java.util.stream.Collectors.toSet());
        return required.stream().anyMatch(actual::contains);
    }
    private static boolean isPublic(HttpServletRequest r) { return r.getRequestURI().startsWith("/api/v1/public/") || ("POST".equals(r.getMethod()) && "/api/v1/auth/google".equals(r.getRequestURI())); }
    private static List<Rule> rule(String method, String path, String permission) { return List.of(new Rule(new AntPathRequestMatcher(path, method), Set.of(permission))); }
    private static List<Rule> any(String path, String... permissions) { return List.of(new Rule(new AntPathRequestMatcher(path), Set.of(permissions))); }
    private static List<Rule> rw(String path, String read, String write) { return List.of(new Rule(new AntPathRequestMatcher(path, "GET"), Set.of(read)), new Rule(new AntPathRequestMatcher(path), Set.of(write))); }
    private record Rule(AntPathRequestMatcher matcher, Set<String> permissions) {}
}
