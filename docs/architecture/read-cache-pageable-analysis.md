# Read caching and pageable architecture

## Scope

This stage introduces bounded local read caching with Caffeine and a shared pageable policy. The objective is to reduce repeated database reads for stable tenant-scoped reference data without caching volatile workflow state or unbounded result pages.

## Cache admission rules

A method may be cached only when all conditions hold:

1. it is read-only;
2. the returned DTO is detached and immutable;
3. the cache key includes the current business tenant;
4. the resource changes infrequently relative to reads;
5. every mutation path can invalidate the corresponding cache;
6. stale data for the configured short TTL does not violate lifecycle, security or financial correctness.

## Initial cache candidates

| Cache | Read methods | TTL | Maximum entries | Invalidation |
|---|---|---:|---:|---|
| `catalogEquipmentType` | `getEquipmentType`, `requireEquipmentType` | 10 min | 5,000 | create/update/archive |
| `catalogJobType` | `getJobType`, `requireJobType` | 10 min | 5,000 | create/update/archive |
| `catalogChecklistTemplate` | `getChecklistTemplate`, `requireChecklistTemplate` | 15 min | 5,000 | create/publish |
| `catalogMaintenanceTemplate` | `getMaintenanceTemplate`, `requireMaintenanceTemplate` | 15 min | 5,000 | create/archive |
| `catalogUnit` | `getUnitOfMeasure` | 15 min | 2,000 | create/update/deactivate |
| `customerReference` | `requireCustomer` | 2 min | 10,000 | create/update/archive |
| `serviceLocationReference` | `requireServiceLocation` | 2 min | 20,000 | create/update/archive |
| `equipmentReference` | `requireEquipment` | 2 min | 20,000 | create/update/archive |
| `identityUserReference` | `requireUser` | 2 min | 10,000 | create/update/activate/deactivate |
| `currentBusiness` | `getCurrentBusiness` | 5 min | 1,000 | create/update/activate/suspend |

All keys use the form `businessId:id`. The current business cache uses `businessId`.

## Explicit cache exclusions

The following data must not be cached in this stage:

- service requests, jobs, visits, executions and work reports;
- maintenance occurrences and due queues;
- notifications, outbox events and data jobs;
- invoices and payments;
- audit and idempotency records;
- mutable attachment or generated-document states;
- pageable result sets;
- authorization decisions.

These resources are volatile, concurrency-sensitive, append-only or operational queues. Local caching would create stale lifecycle decisions or inconsistent multi-node behaviour.

## Pageable policy

All service list methods remain `@Transactional(readOnly = true)` and accept Spring `Pageable`, but pageable input is normalized before repository execution.

Default policy:

- default page: `0`;
- default size: `20`;
- maximum size: `100`;
- unsorted requests receive a deterministic default sort;
- requested sort fields must belong to a method-specific whitelist;
- unsupported properties fail fast with `IllegalArgumentException`;
- negative pages and non-positive sizes are rejected by Spring's pageable implementation before this policy.

Page results are deliberately not cached because page number, size, sort and continuously changing rows create high-cardinality keys and difficult invalidation.

## Multi-node boundary

Caffeine is process-local. This implementation is correct only with short TTL plus explicit local invalidation. It is intended for reference-read acceleration, not cross-node coordination. If the platform later requires immediate cache coherence across replicas, publish domain invalidation events or replace selected caches with a shared cache such as Redis.
