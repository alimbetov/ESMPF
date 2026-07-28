# CRUD, contract and repository remediation

This document tracks the code-grounded remediation sequence before RBAC, JWT bearer runtime and binary file storage.

## Traceability rule

Every externally addressable aggregate must be reviewed across:

```text
Entity
  -> tenant-scoped repository capability
  -> public application-service contract
  -> implementation and transaction/lifecycle rules
  -> REST route, request binding and response status
  -> positive and negative tests
```

A missing generic `delete` method is not automatically a defect. Aggregate lifecycle, auditability, append-only records and legal retention may require archive, revoke, cancel, expire or no mutation at all. Every intentional omission must be documented.

## Confirmed defects

### Identity

- Ordinary User DTOs exposed `passwordHash`, `role`, `externalProvider` and `externalSubject`.
- Ordinary user update could change authorization and authentication binding.
- User response leaked external identity data.
- The current single `role` field conflicts with the planned many-role RBAC model.
- Worker qualification lacks a direct `getQualification` method despite ID-addressed update/expire operations.
- Business location has deactivate but no explicit reactivation operation.

### Documents and files

- Attachment registration accepts client-controlled `storageKey`, size, checksum and content type.
- No multipart upload, binary download, storage port or local/object-storage adapter exists.
- Attachment and attachment-link direct reads are absent despite ID-addressed lifecycle records.
- Attachment link has no unlink/archive policy.
- Document signature is append-only by design, but this must be explicit in API documentation.

### Platform

- Outbox, audit and idempotency lifecycle methods are application/internal capabilities and must not be ordinary REST CRUD.
- Public-token consume by internal UUID is not the eventual public token-resolution contract.
- DataJob and IntegrationConnection lack direct read methods.
- AuditLog is append-only and intentionally has no update/delete.
- DocumentSequence is internal allocation state, not an external entity CRUD API.

### Commercial boundary

- A previous readiness decision deferred invoice/payment API publication, but REST controllers later exposed all CommercialService methods.
- Commercial routes must be disabled or removed until product scope, permissions, reconciliation and payment-provider semantics are approved.

### Controller and repository verification

- Existing controller coverage checks Java method-name parity only; it does not prove route, verb, body binding, status or delegation correctness.
- Tenant-owned services must use business-scoped repository methods for reads and lists.
- Parent/subject ownership must be checked, not merely entity existence.

## Current remediation slice

- Removed credentials, role and external identity from ordinary public User DTOs.
- Prevented MapStruct create/update paths from writing privileged identity fields.
- Reduced `UserReference` to non-authorization identity facts.
- Added an architecture test preventing reintroduction of forbidden fields.

## Planned sequence

### CRUD remediation PR

- complete entity capability matrix;
- add justified missing reads/reactivation/unlink operations;
- internalize infrastructure APIs;
- reconcile commercial REST exposure;
- strengthen route/repository architecture tests.

### PR #15 — RBAC model + API permission matrix

- roles;
- permissions;
- role_permission;
- user_role;
- business scope;
- `@PreAuthorize` / `AuthorizationManager`;
- dedicated role-assignment use cases.

### PR #16 — JWT bearer runtime

- JWT validation;
- user resolution and active check;
- AuthenticatedActor;
- business isolation;
- 401/403 proofs.

### PR #17 — File storage foundation

- multipart upload;
- StoragePort;
- local adapter;
- server-side SHA-256;
- size/type policies;
- upload/download endpoints and authorization.

### PR #18 — MinIO/S3 adapter + quarantine lifecycle

- private object storage;
- quarantine/scanning states;
- controlled download/presigned access;
- cleanup and reconciliation.
