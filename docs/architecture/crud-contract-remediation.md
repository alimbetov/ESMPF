# CRUD, contract and repository remediation

This document tracks the first remediation stage before RBAC, JWT bearer runtime and binary file storage.

## Traceability rule

Every externally addressable aggregate is reviewed across:

```text
Entity
  -> tenant-scoped repository capability
  -> public application-service contract
  -> implementation and lifecycle rules
  -> REST route and request binding
  -> positive and negative tests
```

A missing generic delete is not automatically a defect. Archive, revoke, cancel, expire, immutable or append-only behaviour is documented explicitly.

## Completed in PR #16

### Identity

- removed `passwordHash`, `role`, `externalProvider` and `externalSubject` from ordinary User DTOs;
- prevented MapStruct create/update paths from changing privileged fields;
- added BusinessLocation activation/deactivation symmetry;
- added WorkerQualification point read;
- added negative repeated-transition proofs.

### Documents

- added ReportTemplate point read;
- added Attachment metadata point read and tenant-scoped list;
- added AttachmentLink point read and tenant-scoped unlink;
- added DocumentSignature point read;
- moved document-generation start/complete/fail transitions to `/internal/v1/**`;
- retained metadata-only attachment registration as a transitional boundary until the file-storage PR.

### Service support and platform

- added Material point read;
- added ServiceAgreement point read;
- added WarrantyCase point read;
- added MobileDevice point read;
- added IntegrationConnection point read;
- moved outbox lifecycle, audit append, idempotency lifecycle, data-job execution, integration health callbacks and notification-delivery transitions to `/internal/v1/**`.

### Commercial scope

- removed Invoice and Payment methods from `CommercialRestController`;
- retained Estimate REST as the only current commercial MVP surface;
- kept Invoice and Payment application services dormant for a later product decision.

### Verification

- controller coverage now supports external plus internal controllers and explicit dormant commercial methods;
- REST boundary architecture tests prevent accidental Invoice/Payment publication;
- tenant repository scope architecture test rejects unscoped service-layer repository operations;
- CI is split into fast H2 tests and PostgreSQL/Testcontainers verification.

## Explicitly deferred

- RBAC permissions, role-permission and user-role assignment;
- JWT bearer HTTP runtime;
- object-level authorization;
- multipart upload and binary download;
- Local `StoragePort` adapter;
- MinIO/S3 and quarantine scanning.

## Merge criteria

```text
[ ] fast H2 job green
[ ] PostgreSQL/Testcontainers job green
[ ] controller coverage test green
[ ] REST boundary test green
[ ] tenant repository scope test green
[ ] negative lifecycle tests green
[ ] no Invoice or Payment REST methods
[ ] all worker mutation routes under /internal/v1/**
```

## Follow-up sequence

```text
PR #17  RBAC model + API permission matrix
PR #18  JWT bearer runtime + tenant/object scope
PR #19  Local file storage + upload/download
PR #20  MinIO/S3 + quarantine lifecycle
```
