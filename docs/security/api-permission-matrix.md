# ESMPF operation-level API permission matrix

This matrix is the PR #17 authorization manifest. PR #18 must enforce these declarations after bearer authentication and server-side effective-access resolution.

| HTTP | Route | Permission |
|---|---|---|
| GET | `/api/v1/business` | `BUSINESS_READ` |
| PUT | `/api/v1/business` | `BUSINESS_WRITE` |
| POST | `/api/v1/business/actions/activate` | `BUSINESS_LIFECYCLE` |
| POST | `/api/v1/business/actions/suspend` | `BUSINESS_LIFECYCLE` |
| GET | `/api/v1/business/locations/**` | `LOCATION_READ` |
| POST, PUT | `/api/v1/business/locations/**` | `LOCATION_WRITE` |
| GET | `/api/v1/users/**` | `USER_READ` |
| POST, PUT | `/api/v1/users/**` | `USER_WRITE` |
| POST | `/api/v1/users/{id}/actions/**` | `USER_LIFECYCLE` |
| GET | `/api/v1/**/qualifications/**` | `QUALIFICATION_READ` |
| POST, PUT | `/api/v1/**/qualifications/**` | `QUALIFICATION_WRITE` |
| GET | `/api/v1/access/permissions` | `PERMISSION_READ` |
| GET | `/api/v1/access/roles/**` | `ROLE_READ` |
| POST, PUT | `/api/v1/access/roles/**` | `ROLE_WRITE` |
| GET | `/api/v1/access/users/{id}/role-assignments` | `ROLE_READ` |
| POST | `/api/v1/access/users/{id}/role-assignments` | `ROLE_ASSIGN` |
| POST | `/api/v1/access/role-assignments/{id}/actions/revoke` | `ROLE_ASSIGN` |
| GET | `/api/v1/customers/**` | `CUSTOMER_READ` |
| POST, PUT | `/api/v1/customers/**` | `CUSTOMER_WRITE` |
| POST | `/api/v1/customers/{id}/actions/archive` | `CUSTOMER_ARCHIVE` |
| GET | `/api/v1/customer-interactions/**`, `/api/v1/customers/{id}/interactions` | `CUSTOMER_INTERACTION_READ` |
| POST | `/api/v1/customer-interactions` | `CUSTOMER_INTERACTION_WRITE` |
| GET | `/api/v1/catalog/**` | `CATALOG_READ` |
| POST, PUT | `/api/v1/catalog/**` | `CATALOG_WRITE` |
| POST | `/api/v1/catalog/checklist-templates/{id}/actions/publish` | `CATALOG_PUBLISH` |
| GET | `/api/v1/equipment/**` | `EQUIPMENT_READ` |
| POST, PUT | `/api/v1/equipment` | `EQUIPMENT_WRITE` |
| POST | `/api/v1/equipment/{id}/actions/archive` | `EQUIPMENT_ARCHIVE` |
| POST | `/api/v1/equipment-relations/**` | `EQUIPMENT_RELATION_WRITE` |
| POST | `/api/v1/equipment-issues/**` | `EQUIPMENT_ISSUE_WRITE` |
| POST | `/api/v1/meter-readings` | `METER_READING_WRITE` |
| GET | `/api/v1/maintenance-plans/**` | `MAINTENANCE_PLAN_READ` |
| POST, PUT | `/api/v1/maintenance-plans/**` | `MAINTENANCE_PLAN_WRITE` |
| GET | `/api/v1/maintenance-occurrences/**` | `MAINTENANCE_OCCURRENCE_READ` |
| POST | `/api/v1/maintenance-occurrences/**` | `MAINTENANCE_OCCURRENCE_WRITE` |
| GET | `/api/v1/service-requests/**` | `SERVICE_REQUEST_READ` |
| POST | `/api/v1/service-requests` | `SERVICE_REQUEST_WRITE` |
| POST | `/api/v1/service-requests/{id}/actions/triage` | `SERVICE_REQUEST_WRITE` |
| POST | `/api/v1/service-requests/{id}/actions/accept`, `/reject`, `/cancel` | `SERVICE_REQUEST_DECIDE` |
| POST | `/api/v1/service-requests/{id}/actions/convert-to-job` | `SERVICE_REQUEST_CONVERT` |
| GET | `/api/v1/service-jobs/**` | `SERVICE_JOB_READ` |
| POST | `/api/v1/service-jobs` | `SERVICE_JOB_WRITE` |
| POST | `/api/v1/service-jobs/{id}/actions/mark-ready`, `/schedule` | `SERVICE_JOB_DISPATCH` |
| POST | `/api/v1/service-jobs/{id}/actions/start`, `/hold`, `/resume`, `/complete` | `SERVICE_JOB_EXECUTE` |
| POST | `/api/v1/service-jobs/{id}/actions/close`, `/cancel` | `SERVICE_JOB_CLOSE` |
| GET | `/api/v1/service-jobs/{id}/visits` | `JOB_VISIT_READ` |
| POST | `/api/v1/job-visits` | `JOB_VISIT_PLAN` |
| POST | `/api/v1/job-visits/{id}/actions/**` | `JOB_VISIT_EXECUTE` |
| GET | `/api/v1/job-executions/**` | `WORK_EXECUTION_READ` |
| POST | `/api/v1/job-executions/**` | `WORK_EXECUTION_EXECUTE` |
| GET | `/api/v1/work-reports/**` | `WORK_REPORT_READ` |
| POST | `/api/v1/work-reports` | `WORK_REPORT_WRITE` |
| POST | `/api/v1/work-reports/{id}/actions/approve` | `WORK_REPORT_APPROVE` |
| GET | `/api/v1/equipment/{id}/recommendations` | `RECOMMENDATION_READ` |
| POST | `/api/v1/recommendations/**` | `RECOMMENDATION_WRITE` |
| GET | `/api/v1/materials/**` | `MATERIAL_READ` |
| POST, PUT | `/api/v1/materials/**`, `/api/v1/job-materials` | `MATERIAL_WRITE` |
| GET | `/api/v1/**/service-agreements/**` | `SERVICE_AGREEMENT_READ` |
| POST, PUT | `/api/v1/**/service-agreements/**` | `SERVICE_AGREEMENT_WRITE` |
| GET | `/api/v1/**/warranty-cases/**` | `WARRANTY_READ` |
| POST | `/api/v1/warranty-cases/**` | `WARRANTY_DECIDE` |
| GET, POST | `/api/v1/mobile-devices/**` for own device | `DEVICE_SELF_MANAGE` |
| GET, POST | `/api/v1/users/{id}/mobile-devices/**` for another user | `DEVICE_ADMIN` |
| GET | `/api/v1/estimates/**` | `ESTIMATE_READ` |
| POST, PUT | `/api/v1/estimates` | `ESTIMATE_WRITE` |
| POST | `/api/v1/estimates/{id}/actions/send` | `ESTIMATE_SEND` |
| POST | `/api/v1/estimates/{id}/actions/approve`, `/reject` | `ESTIMATE_DECIDE` |
| GET | `/api/v1/report-templates/**` | `REPORT_TEMPLATE_READ` |
| POST, PUT | `/api/v1/report-templates/**` | `REPORT_TEMPLATE_WRITE` |
| POST | `/api/v1/report-templates/{id}/actions/publish` | `REPORT_TEMPLATE_PUBLISH` |
| GET | `/api/v1/generated-documents/**` | `DOCUMENT_READ` |
| POST | `/api/v1/generated-documents` | `DOCUMENT_GENERATE` |
| POST | `/api/v1/generated-documents/{id}/actions/mark-delivered` | `DOCUMENT_DELIVER` |
| POST, GET | `/api/v1/document-signatures/**` | `DOCUMENT_SIGN` |
| GET | `/api/v1/attachments/**` | `ATTACHMENT_READ` |
| POST, DELETE | `/api/v1/attachment-links/**` | `ATTACHMENT_LINK` |
| POST | `/api/v1/attachments/{id}/actions/**` | `ATTACHMENT_LIFECYCLE` |
| GET | `/api/v1/notification-templates/**` | `NOTIFICATION_TEMPLATE_READ` |
| POST, PUT | `/api/v1/notification-templates/**` | `NOTIFICATION_TEMPLATE_WRITE` |
| POST | `/api/v1/notifications` | `NOTIFICATION_SEND` |
| GET | `/api/v1/notifications` | `NOTIFICATION_READ` |
| GET | `/api/v1/feedback/**` | `FEEDBACK_READ` |
| POST | `/api/v1/feedback/**` | `FEEDBACK_WRITE` |
| GET | `/api/v1/content/articles/**` | `CONTENT_READ` |
| POST, PUT | `/api/v1/content/articles/**` | `CONTENT_WRITE` |
| POST | `/api/v1/content/articles/{id}/actions/schedule`, `/publish`, `/archive` | `CONTENT_PUBLISH` |
| POST | `/api/v1/platform/data-jobs` | `DATA_JOB_CREATE` |
| GET | `/api/v1/platform/data-jobs` | `DATA_JOB_READ` |
| GET | `/api/v1/platform/integrations/**` | `INTEGRATION_READ` |
| POST, PUT | `/api/v1/platform/integrations/**` | `INTEGRATION_WRITE` |
| GET | `/api/v1/platform/audit-events` | `AUDIT_READ` |
| POST | `/api/v1/platform/public-tokens/**` | `PUBLIC_TOKEN_MANAGE` |
| POST | `/api/v1/platform/document-numbers/actions/allocate` | `DOCUMENT_SEQUENCE_ALLOCATE` |

## Exclusions

- `GET /api/v1/public/**` and `POST /api/v1/auth/google` are public.
- `/internal/v1/**` remains denied to user principals and has no tenant RBAC permission.
- Invoice, Payment and Refund permissions do not exist because those REST capabilities remain dormant.
- Object rules shown for device ownership and assigned work are implemented only in PR #18 after the authenticated principal is available.
