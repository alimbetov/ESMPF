# API readiness matrix

| Module / capability | Verdict | Controller stage | Required before publication |
|---|---|---|---|
| Customer CRUD/archive | READY_FOR_API | Core | validation, paging, error mapping, MockMvc |
| Service locations | READY_FOR_API | Core | parent-location negative tests |
| Customer interactions | NEEDS_PROOF | Core/supporting | tenant and related-subject validation proofs |
| Equipment types/job types/UOM | READY_FOR_API | Core | referenced-item archive policy |
| Checklist templates | NEEDS_PROOF | Core | published-template immutability matrix |
| Maintenance templates | NEEDS_PROOF | Core | reference compatibility and archive proofs |
| Equipment CRUD/archive | READY_FOR_API | Core | DTO/API mapping tests |
| Equipment hierarchy/relations | NEEDS_PROOF | Core | self-link, cycle and relation-overlap proofs |
| Equipment issues/readings | READY_FOR_API | Core | append-only and tenant negative tests |
| Maintenance plans | READY_FOR_API | Core | lifecycle HTTP contract |
| Manual occurrences | READY_FOR_API | Core | duplicate-generation error mapping |
| Automatic maintenance generation | BLOCKED_BY_RUNTIME_ADAPTER | Workers | scheduler, locking, catch-up, timezone proof |
| Service requests | READY_FOR_API | Core | full forbidden-transition matrix |
| Service jobs | READY_FOR_API | Core | completion rollback and prerequisite matrix |
| Visits/executions/reports | NEEDS_PROOF | Core | schedule conflict and completion rollback proofs |
| Identity administration | READY_FOR_API | Supporting | API DTOs; later RBAC restrictions |
| Authentication/login | BLOCKED_BY_IMPLEMENTATION | Security | JWT issuer/resource-server design |
| Estimates | READY_FOR_API | Supporting | rounding and duplicate-number HTTP proofs |
| Invoices | NEEDS_PROOF | Supporting | currency and state-transition matrix |
| Payments | NEEDS_REPAIR | Supporting | atomic invoice balance, duplicate external ID, concurrency tests |
| Report templates | READY_FOR_API | Supporting | draft/published immutability proofs |
| Document metadata/status | READY_FOR_API | Supporting | authorization contract |
| Document generation transitions | INTERNAL_ONLY | Workers | no public CRUD/controller |
| Attachment metadata | NEEDS_PROOF | Supporting | storage/checksum/quarantine policy |
| Notification templates/enqueue | READY_FOR_API | Supporting | preference/consent policy |
| Notification delivery transitions | INTERNAL_ONLY | Workers | dispatcher-only contract |
| Customer feedback | READY_FOR_API | Supporting | publication consent tests |
| Public access tokens | NEEDS_PROOF | Supporting/public | token-hash lookup and authorization design |
| Data jobs/outbox/idempotency/sequences | INTERNAL_ONLY | Workers/platform | do not expose generic CRUD |
| Audit query | READY_FOR_API | Supporting/admin | RBAC, filtering and retention |
| Integration connections | NEEDS_PROOF | Supporting/admin | secret-reference redaction and health checks |
| Mobile sync | NEEDS_PROOF | Supporting | idempotency, conflict and replay matrix |

## Controller admission rule

An operation can enter a controller PR only when:

- its application contract is stable;
- tenant ownership is derived from trusted context;
- the main positive and negative paths are proven;
- mutations enforce optimistic version where applicable;
- no known concurrency race remains;
- failures can be mapped to a stable global API error;
- the operation is not worker/internal infrastructure.
