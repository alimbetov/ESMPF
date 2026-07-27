# ESMPF critical defect remediation specification

## 1. Purpose

This change set removes correctness and architectural defects identified in the application-service layer before REST publication.

Target branch:

```text
agent/esmpf-critical-defect-repairs
```

Base:

```text
main
```

The branch must remain isolated from `main` until Maven verification and PostgreSQL integration tests pass.

## 2. Scope

### P0 — mandatory correctness repairs

#### DR-001 Persisted optimistic version in mutation responses

Every mutation returning a versioned DTO must map the response only after Hibernate has flushed the entity update.

Required implementation pattern:

```java
repository.saveAndFlush(entity);
return mapper.toResponse(entity);
```

or:

```java
return mapper.toResponse(repository.saveAndFlush(entity));
```

Acceptance criteria:

- create responses return the persisted version;
- update and lifecycle responses return a version greater than the supplied expected version;
- the returned version can be used immediately by the next mutation;
- stale versions are still rejected;
- transaction rollback remains intact.

#### DR-002 Bounded pagination across all public list operations

Every list operation that accepts `Pageable` must normalize it through `PageablePolicy` before repository execution.

Global rules:

```text
default page = 0
default size = 20
maximum size = 100
deterministic default sort
method-specific sort whitelist
unsupported sort field = IllegalArgumentException
```

Page results must not be cached.

#### DR-003 Main operational-flow proof backlog

The following behaviours remain blockers for REST publication and must be tracked and proven in the next dedicated proof stage:

- duplicate request-to-job conversion;
- rollback of failed request-to-job conversion;
- forbidden request/job/visit/execution/report transitions;
- double start and double completion;
- completion prerequisites;
- scheduling overlap policy;
- PostgreSQL concurrent mutation proofs.

This branch may add architecture guards and focused regressions, but must not expand into REST controllers.

### P1 — content-module consistency repairs

#### DR-004 Content contract registration

`ContentService` must participate in the same architecture checks as every other public application service.

Acceptance criteria:

- no public service accepts `tenantId` or `businessId`;
- no public service exposes internal entity types;
- the registry cannot silently omit newly introduced service contracts.

#### DR-005 Shared application exceptions

Content must use shared exceptions:

```text
com.esmpf.shared.exception.EntityNotFoundException
com.esmpf.shared.exception.StaleEntityException
```

It must not expose persistence-specific exceptions as application semantics.

#### DR-006 Content pageable policy

The administrative and public content feeds must use bounded pagination and explicit sorting rules.

Recommended defaults:

```text
administration: updatedAt DESC, id DESC
public feed: publishedAt DESC, id DESC
```

Allowed administrative fields:

```text
slug, title, type, status, featured, publishAt, publishedAt, visibleUntil, createdAt, updatedAt, id
```

Allowed public fields:

```text
publishedAt, title, type, featured, visibleUntil, id
```

#### DR-007 Tenant-safe content author references

Liquibase must enforce same-tenant references for:

```text
created_by_user_id
published_by_user_id
```

using composite foreign keys `(business_id, user_id)` to the identity user table.

Acceptance criteria:

- non-existent author is rejected;
- author from another tenant is rejected;
- nullable publisher remains valid for draft/scheduled content;
- existing schema remains repeatable.

#### DR-008 Scheduled-publication boundary

`scheduleArticle` currently stores scheduling intent but no worker promotes due articles. Until the worker stage, the capability must be documented as runtime-adapter dependent.

This branch must not pretend automatic publication exists. Add regression documentation/tests around the current behaviour and prepare a narrow internal worker contract in a later stage.

### P2 — cache correctness repairs

#### DR-009 Tenant-scoped invalidation

Catalog mutations currently clear an entire cache for every tenant. Replace broad invalidation where practical with exact tenant/entity invalidation for response and reference keys.

Expected keys:

```text
businessId:id
businessId:id:ref
```

Creating a new item does not require full-cache eviction because no prior ID-based entry exists.

#### DR-010 Cache observability and declared-cache alignment

Declared caches that are not yet used must be explicitly marked as planned or implemented in the corresponding reference query. Avoid configuration that implies a completed capability.

## 3. Non-functional requirements

- Java 21 and Spring Boot baseline must remain unchanged.
- Spring Modulith verification must remain green.
- PostgreSQL Liquibase repeatability must remain green.
- Hibernate `ddl-auto=validate` must pass against PostgreSQL.
- No REST controllers, JWT implementation, payment API or runtime workers are introduced.
- Tenant identity must continue to come only from `TenantContext`.
- Public service DTOs must remain detached from JPA entities.

## 4. Required tests

### Architecture

- all public `*Service` application contracts are discovered or explicitly registered;
- ContentService is included;
- tenant identifiers are absent from public method parameters;
- domain entity types are absent from direct and generic return types.

### Mutation version regression

For representative modules:

```text
Customer
Catalog
Content
```

prove:

1. create returns persisted version;
2. update returns incremented version;
3. returned version succeeds in the next mutation;
4. old version fails.

### Pageable

For representative methods:

- oversized page is capped at 100;
- unsorted request receives deterministic sort;
- unsupported field is rejected;
- Content feeds use the policy;
- existing Catalog policy remains green.

### PostgreSQL

- content author from same tenant succeeds;
- missing author fails;
- cross-tenant author fails;
- Liquibase can be reapplied;
- table count and schema validation remain correct.

### Cache

- point read is cached;
- update/archive invalidates both response and reference keys;
- unrelated tenant key remains cached;
- no pageable result is cached.

## 5. Definition of done

The change set is complete only when:

- all implemented repairs above are reflected in code and tests;
- `mvn --batch-mode --update-snapshots verify` succeeds;
- GitHub Actions is green;
- no known P0 defect from this specification remains in the modified scope;
- PR description lists deferred items separately from completed repairs.
