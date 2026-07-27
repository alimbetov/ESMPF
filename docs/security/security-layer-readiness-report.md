# ESMPF security-layer readiness report

## Decision

JWT is intentionally minimal and identifies only the user:

```text
sub = userId
```

`businessId`, roles and permissions are not trusted token claims. After JWT validation the server loads `UserAccount` and resolves the current business membership, roles and effective permissions from the database or a server-side cache.

## Service inventory

The project exposes 13 public application-service contracts:

1. `CustomerService`
2. `CustomerInteractionService`
3. `CatalogService`
4. `EquipmentService`
5. `MaintenanceService`
6. `ServiceManagementService`
7. `ServiceSupportService`
8. `IdentityService`
9. `CommercialService`
10. `DocumentService`
11. `CommunicationService`
12. `PlatformService`
13. `ContentService`

The existing architecture test proves that public service contracts do not accept `tenantId` or `businessId` from client commands and do not expose internal domain entity types.

## JWT contract

Mandatory claims:

- `iss`
- `aud`
- `sub` — user UUID
- `iat`
- `nbf`
- `exp`
- `jti`
- `token_type=access`
- `ver=1`

The JWT does not contain:

- `business_id`
- roles
- permissions

This avoids stale authorization data and prevents a token claim from becoming the source of truth for tenant membership or RBAC.

## Runtime resolution

```text
validated JWT
  -> userId
  -> UserAccount lookup
  -> active/status validation
  -> business membership resolution
  -> roles and effective permissions resolution
  -> AuthenticatedActor
```

`AuthenticatedActor` is a server-side runtime object containing `userId`, roles and permissions. Business scoping remains an internal persistence concern and is resolved from the authenticated user where required.

## Deliberate boundary

This foundation does not yet install a servlet filter, enable `SecurityFilterChain`, create login/password endpoints or enforce `@PreAuthorize`. The next stage should add `permission`, `role_permission`, `user_role`, effective-permission resolution and then connect bearer-token authentication to the server-side principal resolver.
