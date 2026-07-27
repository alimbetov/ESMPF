# ESMPF security-layer readiness report

## Scope

This change prepares application services for a future Spring Security enforcement layer without enabling HTTP authentication, generated login pages, filters or `SecurityFilterChain` yet.

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

The existing architecture test proves that these contracts do not accept `tenantId` or `businessId` from client commands and do not expose internal domain entity types.

## Existing strengths

- Tenant identity is resolved through `TenantContext` rather than request DTO fields.
- Public contracts are interfaces and expose DTO/value contracts.
- Application implementations use transaction boundaries and tenant-scoped repository methods.
- RBAC has a tenant-scoped role registry and an idempotent provisioning utility.

## Security gaps found

- `TenantContext` supplies identifiers but does not prove that they came from a validated principal.
- There is no common authenticated-actor object containing user, business, roles and permissions.
- There is no strict JWT claim contract or token validation utility.
- There is no reusable application-layer guard for tenant, role and permission checks.
- `permission`, `role_permission` and `user_role` are not implemented yet.
- HTTP authentication and authorization enforcement are intentionally absent.

## Foundation introduced

### `AuthenticatedActor`

Trusted immutable identity with:

- `userId`
- `businessId`
- normalized roles
- normalized permissions

### `AuthenticatedActorContext`

Security-ready extension of `TenantContext`. Existing services can continue using `requireBusinessId()` and `requireUserId()`, while a future authentication filter supplies a validated actor.

### `AuthorizationGuard`

Reusable checks for:

- same-business access
- required role
- required permission
- any-of permissions

### `JwtUtility`

HMAC SHA-256 access-token issue and validation with mandatory claims:

- `iss`
- `aud`
- `sub` — user UUID
- `business_id`
- `roles`
- `permissions`
- `iat`
- `nbf`
- `exp`
- `jti`
- `token_type=access`
- `ver=1`

Validation rejects invalid signatures, algorithms, issuer, audience, token type, version, timestamps and UUID claims. Secrets shorter than 32 UTF-8 bytes are rejected. Production secrets must be supplied from an external secret store and must never be committed.

## Service permission map for the next stage

| Contract | Initial permission families |
|---|---|
| CustomerService | `CUSTOMER_READ`, `CUSTOMER_CREATE`, `CUSTOMER_UPDATE`, `CUSTOMER_ARCHIVE` |
| CustomerInteractionService | `CUSTOMER_INTERACTION_READ`, `CUSTOMER_INTERACTION_MANAGE` |
| CatalogService | `CATALOG_READ`, `CATALOG_MANAGE` |
| EquipmentService | `EQUIPMENT_READ`, `EQUIPMENT_MANAGE` |
| MaintenanceService | `MAINTENANCE_READ`, `MAINTENANCE_MANAGE` |
| ServiceManagementService | `REQUEST_*`, `JOB_*`, `VISIT_*` |
| ServiceSupportService | `SERVICE_SUPPORT_READ`, `SERVICE_SUPPORT_MANAGE` |
| IdentityService | `USER_READ`, `USER_MANAGE`, `ROLE_READ`, `ROLE_MANAGE` |
| CommercialService | `COMMERCIAL_READ`, `COMMERCIAL_MANAGE` |
| DocumentService | `DOCUMENT_READ`, `DOCUMENT_MANAGE` |
| CommunicationService | `COMMUNICATION_READ`, `COMMUNICATION_SEND` |
| PlatformService | `PLATFORM_READ`, `PLATFORM_MANAGE` |
| ContentService | `CONTENT_READ`, `CONTENT_MANAGE`, `CONTENT_PUBLISH` |

The exact method-to-permission matrix should be added together with complete RBAC assignment tables, not guessed inside service implementations prematurely.

## Deliberate boundary

This foundation does **not** yet:

- accept bearer tokens over HTTP;
- install a servlet filter;
- enable Spring Security auto-configuration;
- create login/password endpoints;
- enforce annotations such as `@PreAuthorize`;
- persist permission or user-role assignments.

The next security PR should implement `permission`, `role_permission`, `user_role`, effective-permission resolution, and then wire a bearer-token filter to `AuthenticatedActorContext` before enabling endpoint enforcement.
