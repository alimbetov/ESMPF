# ESMPF RBAC domain implementation specification

## 1. Purpose

Introduce a tenant-scoped role and permission foundation without adding Spring Security, JWT parsing, HTTP authorization filters or `@PreAuthorize` rules.

This stage defines who has which business capabilities. A later security stage will authenticate callers and apply the effective permissions.

## 2. Scope

### Included

- tenant-scoped roles;
- global permission catalogue;
- role-to-permission relations;
- user-to-role assignments;
- effective permission query contract;
- deterministic system-role provisioning;
- idempotent utility for test and bootstrap scenarios;
- Liquibase constraints and PostgreSQL proofs;
- application-level tests.

### Excluded

- Spring Security dependencies;
- `SecurityFilterChain`;
- OAuth2 Resource Server and JWT validation;
- login/password endpoints;
- HTTP 401/403 mapping;
- CORS;
- payment and refund permissions.

## 3. Core model

```text
Business
  -> AccessRole
  -> RolePermission -> Permission
  -> UserRole -> UserAccount
```

### Permission

Permission is a global application-defined capability with a stable code such as:

```text
CUSTOMER_READ
CUSTOMER_WRITE
EQUIPMENT_READ
EQUIPMENT_WRITE
REQUEST_MANAGE
JOB_DISPATCH
JOB_EXECUTE
WORK_REPORT_APPROVE
CATALOG_MANAGE
USER_MANAGE
ROLE_MANAGE
AUDIT_READ
```

Permission codes are immutable after publication.

### AccessRole

A role belongs to one business tenant.

Required fields:

```text
businessId
code
name
description
system
active
version
```

Constraints:

- unique `(business_id, lower(code))`;
- unique `(business_id, id)`;
- system role code cannot be renamed;
- inactive role contributes no effective permissions.

### UserRole

A user may have multiple roles. Assignment must reference a user and role from the same tenant.

Future-compatible scope fields:

```text
scopeType: TENANT | BUSINESS_LOCATION | OWN_ASSIGNMENTS
scopeId
validFrom
validUntil
active
```

### RolePermission

Associates an active permission with a tenant role. Deny semantics are excluded; the model is allow-only.

## 4. Initial system roles

```text
OWNER
ADMIN
DISPATCHER
SUPERVISOR
TECHNICIAN
VIEWER
```

System roles are deterministic, cannot be deleted and are provisioned per tenant.

The last active OWNER assignment must not be revocable.

## 5. Role provisioning utility

Create a reusable component:

```text
RoleProvisioningService
```

Required operation:

```java
AccessRoleReference ensureRole(
    UUID businessId,
    String code,
    String name,
    String description,
    boolean system
)
```

Behaviour:

1. validate and normalize input;
2. search by `(businessId, code)` case-insensitively;
3. return the existing role without creating duplicates;
4. create and flush a new active role when absent;
5. recover from a concurrent duplicate insert by re-reading the role;
6. never update an existing role implicitly;
7. remain transactional and tenant-safe.

The utility is intended for:

- integration test fixtures;
- MockMvc test setup;
- tenant bootstrap;
- future migration/bootstrap jobs.

It must not depend on Spring Security.

## 6. Required tests

### Provisioning utility

- creates a missing role;
- second call returns the same role ID;
- code matching is case-insensitive;
- role is created for the requested tenant only;
- same code may exist in another tenant;
- blank or malformed code is rejected;
- concurrent provisioning produces one row.

### PostgreSQL

- tenant-scoped unique code is enforced;
- composite foreign keys reject cross-tenant role assignments;
- Liquibase reapplication is safe;
- Hibernate schema validation passes.

### Architecture

- RBAC services do not accept tenant identifiers from external command DTOs except the explicit bootstrap/provisioning utility;
- RBAC entities are not exposed through public service contracts;
- no Spring Security dependency is introduced.

## 7. Migration strategy

The legacy `user_account.role` column remains temporarily for backward compatibility. It becomes deprecated and must not be used as the source of effective permissions after UserRole is introduced.

Migration sequence:

1. create RBAC tables;
2. provision system roles for each business;
3. map existing `user_account.role` values into `user_role`;
4. switch permission reads to RBAC tables;
5. remove the legacy column in a later migration after verification.

## 8. Definition of done

- RBAC tables and constraints exist;
- system role codes are documented;
- `RoleProvisioningService` is implemented and idempotent;
- tests prove create/reuse/tenant isolation;
- PostgreSQL Testcontainers remain green;
- Spring Modulith verification remains green;
- `mvn --batch-mode --update-snapshots verify` succeeds;
- Spring Security is not added.
