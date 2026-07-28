# ESMPF REST controller architecture

## Scope

This layer exposes every public application-service method through Spring MVC without moving business logic into controllers.

## Conventions

- API prefix: `/api/v1`
- create operations: `POST`, normally `201 Created`
- asynchronous enqueue/generation operations: `202 Accepted`
- reads: `GET`
- draft/resource updates: `PUT`
- lifecycle transitions: `POST /{resource}/{id}/actions/{action}`
- lists: Spring Data `Page<T>` with `Pageable`
- optimistic locking: `version` in command DTOs or action request bodies
- no tenant/business identifier is accepted as a trusted security field
- controllers delegate directly to application-service interfaces

## Resource groups

- `/auth`
- `/business`, `/users`, `/qualifications`
- `/customers`, `/service-locations`, `/customer-interactions`
- `/catalog/*`
- `/equipment`, `/equipment-relations`, `/equipment-issues`, `/meter-readings`
- `/maintenance-plans`, `/maintenance-occurrences`
- `/service-requests`, `/service-jobs`, `/job-visits`, `/job-executions`, `/work-reports`
- `/recommendations`, `/materials`, `/service-agreements`, `/warranty-cases`, `/mobile-devices`, `/sync-operations`
- `/estimates`, `/invoices`, `/payments`
- `/report-templates`, `/generated-documents`, `/attachments`, `/document-signatures`
- `/notification-templates`, `/notifications`, `/feedback`
- `/platform/*`
- `/content/articles`, `/public/articles`

## Error contract

`ApiExceptionHandler` returns RFC 9457-compatible `ProblemDetail` responses:

- `400` malformed/invalid request
- `403` authenticated actor lacks access
- `404` resource not found
- `409` invalid lifecycle state or optimistic-lock conflict

Authentication failures (`401`) will be provided by the future Spring Security `AuthenticationEntryPoint`.

## Security boundary

This PR defines routes but does not enable `SecurityFilterChain` or method security. The next security phase should assign route families to permissions, then add bearer authentication and `401/403` MockMvc proofs.

## Coverage proof

`RestControllerCoverageArchitectureTests` verifies that every method declared by all application-service contracts, including `AuthenticationService`, has a corresponding controller method with the same operation name.
