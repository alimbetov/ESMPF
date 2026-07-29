# ESMPF Foundation 1.0 — Official Readiness Gate

ESMPF Foundation 1.0 is ready only when the following five end-to-end chains pass without manual SQL, direct repository manipulation, or skipped lifecycle transitions.

## E2E-01 Service lifecycle

Customer → Service Location → Equipment → Service Request → Service Job → Visit → Checklist Execution → Work Report → PDF request → Job completion → Job closure → Equipment history.

## E2E-02 Maintenance lifecycle

Maintenance Template → Maintenance Plan → Occurrence generation → Service Job linkage → Standard service execution → Occurrence completion → Equipment history.

## E2E-03 File lifecycle

Upload → metadata persistence → attachment to business object → download → soft delete → download rejection → restore → download.

## E2E-04 Commercial lifecycle

Estimate → send → approve → invoice → issue → partial payment → final payment → PAID status.

External payment-provider integration is not required. Manual registration and confirmation are sufficient.

## E2E-05 Administration lifecycle

Business → user → role → permission assignment → authentication → allowed operation → forbidden operation → role revocation.

## Cross-cutting requirements

- Tenant isolation for reads, writes, actions, and file downloads.
- Optimistic locking for mutable aggregates.
- Named lifecycle actions instead of arbitrary status mutation.
- PostgreSQL Liquibase reapplication and Hibernate validation.
- Consistent 400/401/403/404/409/422 API behavior.
- No manual database intervention to complete a scenario.

## Scope boundary

The following are not release blockers for Foundation 1.0: customer portal, S3, antivirus, distributed cache, refresh-token families, payment gateways, tax engines, ERP integration, predictive maintenance, route optimization, IoT, BI, and multi-region deployment.

## Release decision

A release candidate may be labelled `ESMPF Foundation 1.0` only when automated tests prove all five chains and the readiness matrix contains no P0 gaps.
