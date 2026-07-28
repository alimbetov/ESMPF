# PR #18 — JWT bearer runtime and proven object scope

## Runtime identity pipeline

```text
Authorization: Bearer <ESMPF access token>
  -> HS256 signature, issuer, audience, type, version and time validation
  -> sub parsed as internal userId
  -> AccessControlQuery resolves active persisted user, active Business, roles and permissions
  -> immutable EsmpfPrincipal
  -> Spring SecurityContext
  -> SecurityPrincipalContext supplies businessId and userId
  -> operation permission filter
  -> resource-specific object policy
```

Only `sub` is accepted from the access token. Claims attempting to provide `businessId`, roles or permissions have no effect.

## Configuration

JWT runtime is disabled by default. Production enables it through `ESMPF_JWT_ENABLED=true` and must provide `ESMPF_JWT_SECRET` containing at least 32 UTF-8 bytes. Issuer, audience, access-token TTL and clock skew are validated at startup.

The access token TTL is limited to one hour. ESMPF does not claim per-token revocation in Foundation 1.0. User activity, Business status, role assignments and permissions are evaluated from persisted state for every authenticated request.

## Tenant context

`SecurityPrincipalContext` reads the immutable principal directly from `SecurityContextHolder`; it does not copy tenant data into a mutable singleton or independent ThreadLocal. Request clients cannot supply `businessId` through headers, path variables, query parameters, DTOs or JWT claims.

## Permission enforcement

Protected `/api/v1/**` routes require an explicit operation rule. An authenticated route without a declared rule is denied. Public routes are limited to published content and Google authentication. `/internal/**` and protected platform worker routes remain denied to user JWTs.

## Proven object policy

The only object-level rule enabled in this PR is mobile-device ownership because `MobileDevice.userId` is a typed persisted relation:

- owner requires `DEVICE_SELF_MANAGE` or `DEVICE_ADMIN`;
- another user's device requires `DEVICE_ADMIN`;
- tenant repository lookup prevents cross-Business discovery.

Technician job/visit scope is deliberately deferred. Current worker collections are partly JSON-backed and are not accepted as authorization evidence. A typed assignment model is required before those relations can authorize execution.

## Excluded

- refresh tokens;
- distributed token blacklist;
- permission caching;
- service-to-service authentication;
- generic scope metadata;
- PostgreSQL RLS;
- customer portal identity;
- JSON-based technician authorization.
