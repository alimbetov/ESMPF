# ESMPF 1.0 entity catalog

## Identity — 4
- `Business`
- `BusinessLocation`
- `UserAccount`
- `WorkerQualification`

## Customer — 3
- `Customer`
- `CustomerInteraction`
- `ServiceLocation`

## Catalog — 5
- `EquipmentType`
- `JobType`
- `ChecklistTemplate`
- `MaintenanceTemplate`
- `UnitOfMeasure`

## Equipment — 4
- `Equipment`
- `EquipmentRelation`
- `EquipmentIssue`
- `MeterReading`

## Maintenance — 2
- `MaintenancePlan`
- `MaintenanceOccurrence`

## Service — 12
- `ServiceRequest`
- `ServiceJob`
- `JobVisit`
- `JobExecution`
- `WorkReport`
- `Recommendation`
- `MaterialCatalogItem`
- `JobMaterial`
- `ServiceAgreement`
- `WarrantyCase`
- `MobileDevice`
- `SyncOperation`

## Commercial — 3
- `Estimate`
- `Invoice`
- `Payment`

## Document — 5
- `ReportTemplate`
- `GeneratedDocument`
- `DocumentSignature`
- `Attachment`
- `AttachmentLink`

## Communication — 3
- `NotificationTemplate`
- `Notification`
- `CustomerFeedback`

## Platform — 7
- `PublicAccessToken`
- `DataJob`
- `OutboxEvent`
- `AuditLog`
- `IdempotencyRecord`
- `IntegrationConnection`
- `DocumentSequence`

**Total: 48 persistent entities.**

Cross-module references are stored as UUID values in this baseline. Repositories and application services will enforce tenant ownership and domain invariants in the next implementation stage.
