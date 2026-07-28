# Tenant Isolation Inventory

## Rule

Tenant-owned data must be accessed with a `businessId` predicate at repository or application-service boundary. Direct identifier-only repository operations are prohibited unless explicitly listed below.

## Approved global exceptions

1. Immutable global permission catalogue lookup by permission code.
2. Global reference tables explicitly documented as non-tenant data.
3. Persisted actor bootstrap lookup of `UserAccount` by globally unique internal `userId`; the resolved `businessId` must then be validated and propagated through `EsmpfPrincipal`.

## Required tenant-owned aggregates

- Customer
- Equipment
- ServiceRequest
- ServiceJob
- JobVisit
- WorkExecution
- WorkReport
- MobileDevice
- SyncOperation
- ServiceAgreement
- WarrantyCase
- Estimate
- Attachment
- Document
- AccessRole
- UserRoleAssignment

## Prohibited repository patterns

For tenant-owned aggregates, production code must not use:

- `findById(id)`
- `existsById(id)`
- `deleteById(id)`
- `getReferenceById(id)`
- JPQL or native SQL without `businessId`
- bulk update/delete without `businessId`

## Expected cross-tenant behavior

- Object lookup from another Business returns `404`.
- Cross-tenant reference creation returns `404` or a non-disclosing domain validation error.
- List and search operations never return another Business's records.
- Authorization responses must not reveal existence through inconsistent status codes or details.
