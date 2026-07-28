# ESMPF route matrix

This document fixes the PR #16 route boundary. The `Permission` column is intentionally reserved for PR #17.

| Route family | Exposure | Authentication | Permission |
|---|---|---|---|
| `GET /api/v1/public/**` | Public | No | Public |
| `POST /api/v1/auth/google` | Public authentication | No | Public |
| `/api/v1/business/**` | External business API | Yes | PR17 |
| `/api/v1/users/**` | External administration | Yes | PR17 |
| `/api/v1/customers/**` | External business API | Yes | PR17 |
| `/api/v1/equipment/**` | External business API | Yes | PR17 |
| `/api/v1/service-requests/**` | External business API | Yes | PR17 |
| `/api/v1/service-jobs/**` | External business API | Yes | PR17 |
| `/api/v1/estimates/**` | External MVP commercial API | Yes | PR17 |
| `/api/v1/invoices/**` | Disabled | N/A | Deferred |
| `/api/v1/payments/**` | Disabled | N/A | Deferred |
| `/api/v1/report-templates/**` | External administration | Yes | PR17 |
| `/api/v1/generated-documents/**` reads/request | External business API | Yes | PR17 |
| `/api/v1/attachments/**` metadata | External transitional API | Yes | PR17; content PR19 |
| `/api/v1/notifications` enqueue/list | External business API | Yes | PR17 |
| `/api/v1/platform/data-jobs` create/list | External request/status API | Yes | PR17 |
| `/api/v1/platform/integrations/**` administration | External administration | Yes | PR17 |
| `/internal/v1/platform/outbox-events/**` | Internal worker | Denied to normal principals | Internal |
| `/internal/v1/platform/audit-events` append | Internal worker | Denied to normal principals | Internal |
| `/internal/v1/platform/idempotency-records/**` | Internal worker | Denied to normal principals | Internal |
| `/internal/v1/platform/data-jobs/**/actions/**` | Internal worker | Denied to normal principals | Internal |
| `/internal/v1/platform/integrations/**/actions/record-*` | Internal callback | Denied to normal principals | Internal |
| `/internal/v1/notifications/**/actions/**` | Internal worker | Denied to normal principals | Internal |
| `/internal/v1/generated-documents/**/actions/**` | Internal worker | Denied to normal principals | Internal |

## Contract rules

- External controllers expose business use cases only.
- Internal controllers are owned by the module whose service they invoke.
- No central cross-module worker controller is allowed.
- Tenant/business IDs are not accepted as trusted request parameters.
- PR #17 must replace every `PR17` marker with a concrete `PermissionCode`.
