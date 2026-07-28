# ESMPF entity capability matrix

This matrix classifies persistent entities by intended behavior. Missing operations are deliberate only when explicitly marked.

Legend:

- `YES` — externally available business operation.
- `INTERNAL` — application/worker contract only; not part of the frontend REST API.
- `LIFECYCLE` — explicit state transition replaces generic update/delete.
- `APPEND` — append-only or immutable after creation.
- `DEFERRED` — excluded from the current MVP.
- `N/A` — operation is not valid for the entity type.

| Module | Entity | Type | Create | Get | List | Update | Lifecycle | Archive/Deactivate | Delete | Exposure |
|---|---|---|---|---|---|---|---|---|---|---|
| Identity | Business | Mutable root | YES | current | N/A | YES | activate/suspend | N/A | NO | External administration |
| Identity | BusinessLocation | Mutable aggregate | YES | YES | YES | YES | activate/deactivate | deactivate | NO | External administration |
| Identity | UserAccount | Mutable aggregate | YES | YES | YES | profile only | activate/deactivate | deactivate | NO | External administration; roles and identities excluded |
| Identity | WorkerQualification | Lifecycle aggregate | YES | YES | by user | YES | expire | NO | NO | External administration |
| Customer | Customer | Mutable aggregate | YES | YES | YES | YES | N/A | archive | NO | External |
| Customer | ServiceLocation | Mutable aggregate | YES | YES | by customer | YES | N/A | archive | NO | External |
| Customer | CustomerInteraction | Append-only | APPEND | YES | by customer | NO | N/A | NO | NO | External history |
| Catalog | EquipmentType | Mutable reference | YES | YES | YES | YES | N/A | deactivate where supported | NO | External administration |
| Catalog | JobType | Mutable reference | YES | YES | YES | YES | N/A | deactivate where supported | NO | External administration |
| Catalog | ChecklistTemplate | Lifecycle aggregate | YES | YES | YES | draft only | publish/archive | archive | NO | External administration |
| Catalog | MaintenanceTemplate | Mutable reference | YES | YES | YES | YES | N/A | deactivate where supported | NO | External administration |
| Catalog | UnitOfMeasure | Mutable reference | YES | YES | YES | YES | N/A | deactivate where supported | NO | External administration |
| Equipment | Equipment | Mutable aggregate | YES | YES | YES | YES | N/A | archive | NO | External |
| Equipment | EquipmentRelation | Association | YES | YES where addressable | by equipment | limited | unlink | N/A | scoped delete | External |
| Equipment | EquipmentIssue | Lifecycle aggregate | YES | YES | by equipment | limited | explicit states | close/archive | NO | External |
| Equipment | MeterReading | Append-only | APPEND | YES where addressable | by equipment | NO | N/A | NO | NO | External history |
| Maintenance | MaintenancePlan | Lifecycle aggregate | YES | YES | YES | draft/allowed states | activate/suspend/close | close | NO | External |
| Maintenance | MaintenanceOccurrence | Lifecycle aggregate | INTERNAL/generate | YES where addressable | queues | NO | link/complete/cancel | NO | NO | Worker and operational API |
| Service | ServiceRequest | Lifecycle aggregate | YES | YES | YES | limited | triage/accept/reject/cancel/convert | terminal states | NO | External |
| Service | ServiceJob | Lifecycle aggregate | create/convert | YES | YES | limited | ready/schedule/start/wait/complete/close/cancel | terminal states | NO | External |
| Service | JobVisit | Lifecycle aggregate | YES | YES | by job | limited | plan/start/complete/cancel | terminal states | NO | External |
| Service | JobExecution | Immutable snapshot | YES | YES | by job | execution-only | complete | NO | NO | External operational |
| Service | WorkReport | Lifecycle aggregate | YES | YES | by job | draft only | approve | NO | NO | External operational |
| Service | Recommendation | Lifecycle aggregate | YES | YES where addressable | by equipment | NO | convert/dismiss | terminal states | NO | External |
| Service | MaterialCatalogItem | Mutable reference | YES | YES | YES | YES | activate/deactivate | deactivate | NO | External administration |
| Service | JobMaterial | Append-oriented | YES | YES where addressable | by job | limited/deferred | N/A | NO | NO | External operational |
| Service | ServiceAgreement | Lifecycle aggregate | YES | YES | by customer | draft only | activate/suspend/close | close | NO | External |
| Service | WarrantyCase | Lifecycle aggregate | YES | YES | by equipment | NO | approve/reject/close | close | NO | External |
| Service | MobileDevice | Security lifecycle | register | YES | by user | touch only | revoke | revoke | NO | External self/admin |
| Service | SyncOperation | Internal process | INTERNAL | INTERNAL | INTERNAL | NO | receive/complete/fail | NO | NO | Internal worker |
| Commercial | Estimate | Lifecycle aggregate | YES | YES | YES | draft only | send/approve/reject | terminal states | NO | External MVP quotation |
| Commercial | Invoice | Lifecycle aggregate | DEFERRED | DEFERRED | DEFERRED | DEFERRED | DEFERRED | DEFERRED | NO | Not exposed in current MVP |
| Commercial | Payment | Lifecycle aggregate | DEFERRED | DEFERRED | DEFERRED | NO | DEFERRED | DEFERRED | NO | Not exposed in current MVP |
| Document | ReportTemplate | Lifecycle aggregate | YES | YES | YES | draft only | publish/archive | archive | NO | External administration |
| Document | GeneratedDocument | Process + immutable output | request | YES | YES | NO | INTERNAL start/complete/fail; external delivery state as approved | supersede | NO | Mixed; worker transitions internal |
| Document | Attachment | Storage-owned metadata | PR19 | YES | YES | metadata internal | quarantine/archive | archive | NO | Metadata external; content PR19 |
| Document | AttachmentLink | Association | YES | YES | by attachment | NO | unlink | N/A | scoped delete | External |
| Document | DocumentSignature | Append-only evidence | APPEND | YES | by document | NO | future invalidation only | NO | NO | External evidence |
| Communication | NotificationTemplate | Lifecycle aggregate | YES | YES where addressable | YES | draft only | activate/archive | archive | NO | External administration |
| Communication | Notification | Internal process | enqueue business action | INTERNAL | business status only if needed | NO | INTERNAL sending/sent/fail | terminal states | NO | Worker lifecycle internal |
| Communication | CustomerFeedback | Lifecycle aggregate | YES | YES where addressable | YES | NO | respond/resolve/reject | terminal states | NO | External |
| Content | NewsArticle | Lifecycle aggregate | YES | public/admin reads | YES | draft/allowed | schedule/publish/archive | archive | NO | Public/admin |
| Platform | PublicAccessToken | Security lifecycle | INTERNAL/admin | INTERNAL | INTERNAL | NO | consume/revoke | revoke | NO | Internal/trusted endpoint |
| Platform | DataJob | Internal process | request where applicable | status only if product-visible | list if product-visible | NO | INTERNAL start/progress/complete/fail | terminal states | NO | Worker lifecycle internal |
| Platform | OutboxEvent | Append-only process | INTERNAL | INTERNAL | INTERNAL | NO | INTERNAL claim/publish/fail | NO | NO | Internal only |
| Platform | AuditLog | Append-only evidence | INTERNAL | permission-gated/deferred | permission-gated | NO | N/A | NO | NO | Internal/audit API only |
| Platform | IdempotencyRecord | Internal process | INTERNAL | INTERNAL | INTERNAL | NO | INTERNAL complete/fail | expiry cleanup | NO | Internal only |
| Platform | IntegrationConnection | Mutable lifecycle | YES | YES | YES | inactive only | activate/suspend | suspend | NO | External administration; health updates internal |
| Platform | DocumentSequence | Internal counter | INTERNAL | INTERNAL | INTERNAL | atomic allocation | N/A | NO | NO | Internal only |
| RBAC | AccessRole | Mutable security aggregate | PR17 | PR17 | PR17 | PR17 | activate/deactivate | deactivate | NO | Security administration |

## Mandatory architectural rules

1. Tenant-owned reads and mutations use tenant-scoped repository queries.
2. Ordinary User DTOs never contain password hashes, roles, permissions or external identity values.
3. Append-only evidence records are never generically updated or deleted.
4. Internal process transitions are not published under the external `/api/v1` frontend namespace.
5. Invoice and Payment remain dormant until the commercial feature boundary is explicitly reopened.
6. Attachment metadata registration is transitional only; PR #19 replaces it with server-owned storage creation.
