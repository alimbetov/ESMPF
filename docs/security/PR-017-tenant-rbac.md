# PR #17 — Tenant-wide RBAC and permission matrix

## Decision

ESMPF uses allow-only, tenant-wide RBAC.

```text
UserAccount
  -> UserRoleAssignment
  -> AccessRole
  -> RolePermission
  -> PermissionCode
```

Permissions are application-owned immutable capabilities. Roles and assignments belong to one Business tenant. Generic `scopeType/scopeId`, location scopes, own-assignment scopes, JWT bearer parsing and object-level authorization are explicitly excluded from PR #17.

## Security invariants

1. Permission codes are defined by `PermissionCode` and seeded deterministically.
2. A role code is unique inside one tenant.
3. System role codes are immutable and system roles cannot be deleted.
4. OWNER cannot be deactivated and must retain the complete permission catalogue.
5. User-role assignments are append-oriented and revoked through an explicit lifecycle action.
6. Database composite foreign keys reject cross-tenant assignments.
7. Only one ACTIVE assignment may exist for the same tenant, user and role.
8. The last effective OWNER assignment is protected by a pessimistic Business lock.
9. Revoked, expired and future assignments do not contribute permissions.
10. Inactive users, inactive roles and suspended businesses do not produce effective access.
11. `user_account.role` is migration input only and must not be used for runtime authorization.
12. JWT claims containing business, roles or permissions remain untrusted; PR #18 resolves access server-side.

## System roles

- `OWNER` — fixed, complete tenant control.
- `ADMIN` — tenant administration template.
- `DISPATCHER` — request and dispatch template.
- `SUPERVISOR` — operational supervision template.
- `TECHNICIAN` — service execution template.
- `VIEWER` — read-only template.

Custom tenant roles are supported. Except for OWNER invariants, authorization decisions must use permission codes rather than hard-coded role checks.

## Migration

The migration:

1. creates `permission`, `role_permission` and `user_role_assignment`;
2. adds cross-tenant and lifecycle constraints;
3. adds uniqueness for external identity provider and subject;
4. rejects unknown legacy role codes;
5. provisions system roles for existing businesses;
6. assigns all permissions to OWNER and read permissions to VIEWER;
7. maps legacy `USER` to `VIEWER` and known role codes to matching assignments.

## Deferred to PR #18

- bearer-token authentication;
- `SecurityContext` principal;
- security-backed `TenantContext`;
- runtime permission enforcement;
- object-level policies;
- internal service authentication;
- permission caching;
- refresh-token and token-revocation infrastructure.
