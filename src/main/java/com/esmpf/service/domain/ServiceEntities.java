package com.esmpf.service.domain;

import com.esmpf.shared.persistence.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "service_request", indexes = @Index(name = "idx_service_request_business", columnList = "business_id"))
class ServiceRequest extends TenantEntity {
    @Column(name = "customer_id") private UUID customerId;
    @Column(name = "service_location_id") private UUID serviceLocationId;
    @Column(name = "equipment_id") private UUID equipmentId;
    @Column(name = "source") private String source;
    @Column(name = "priority") private String priority;
    @Column(name = "summary") private String summary;
    @Column(name = "description", columnDefinition = "text") private String description;
    @Column(name = "status") private String status;
    @Column(name = "requested_at") private Instant requestedAt;
    @Column(name = "requested_by") private UUID requestedBy;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "service_job", indexes = @Index(name = "idx_service_job_business", columnList = "business_id"))
class ServiceJob extends TenantEntity {
    @Column(name = "request_id") private UUID requestId;
    @Column(name = "maintenance_occurrence_id") private UUID maintenanceOccurrenceId;
    @Column(name = "customer_id") private UUID customerId;
    @Column(name = "service_location_id") private UUID serviceLocationId;
    @Column(name = "equipment_id") private UUID equipmentId;
    @Column(name = "job_type_id") private UUID jobTypeId;
    @Column(name = "service_agreement_id") private UUID serviceAgreementId;
    @Column(name = "status") private String status;
    @Column(name = "priority") private String priority;
    @Column(name = "title") private String title;
    @Column(name = "description", columnDefinition = "text") private String description;
    @Column(name = "planned_start") private Instant plannedStart;
    @Column(name = "planned_end") private Instant plannedEnd;
    @Column(name = "lead_worker_id") private UUID leadWorkerId;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "assigned_worker_ids_json", columnDefinition = "jsonb") private String assignedWorkerIdsJson;
    @Column(name = "blocked_reason") private String blockedReason;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "job_visit", indexes = @Index(name = "idx_job_visit_business", columnList = "business_id"))
class JobVisit extends TenantEntity {
    @Column(name = "job_id") private UUID jobId;
    @Column(name = "scheduled_start") private Instant scheduledStart;
    @Column(name = "scheduled_end") private Instant scheduledEnd;
    @Column(name = "actual_start") private Instant actualStart;
    @Column(name = "actual_end") private Instant actualEnd;
    @Column(name = "status") private String status;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "worker_ids_json", columnDefinition = "jsonb") private String workerIdsJson;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "arrival_data_json", columnDefinition = "jsonb") private String arrivalDataJson;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "completion_data_json", columnDefinition = "jsonb") private String completionDataJson;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "customer_confirmation_json", columnDefinition = "jsonb") private String customerConfirmationJson;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "job_execution", indexes = @Index(name = "idx_job_execution_business", columnList = "business_id"))
class JobExecution extends TenantEntity {
    @Column(name = "job_id") private UUID jobId;
    @Column(name = "visit_id") private UUID visitId;
    @Column(name = "checklist_template_id") private UUID checklistTemplateId;
    @Column(name = "template_version") private Integer templateVersion;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "schema_snapshot_json", columnDefinition = "jsonb") private String schemaSnapshotJson;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "answers_json", columnDefinition = "jsonb") private String answersJson;
    @Column(name = "started_at") private Instant startedAt;
    @Column(name = "completed_at") private Instant completedAt;
    @Column(name = "completed_by") private UUID completedBy;
    @Column(name = "status") private String status;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "work_report", indexes = @Index(name = "idx_work_report_business", columnList = "business_id"))
class WorkReport extends TenantEntity {
    @Column(name = "job_id") private UUID jobId;
    @Column(name = "visit_id") private UUID visitId;
    @Column(name = "job_execution_id") private UUID jobExecutionId;
    @Column(name = "diagnosis", columnDefinition = "text") private String diagnosis;
    @Column(name = "work_performed", columnDefinition = "text") private String workPerformed;
    @Column(name = "result", columnDefinition = "text") private String result;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "materials_summary_json", columnDefinition = "jsonb") private String materialsSummaryJson;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "measurements_summary_json", columnDefinition = "jsonb") private String measurementsSummaryJson;
    @Column(name = "customer_comment") private String customerComment;
    @Column(name = "completed_by") private UUID completedBy;
    @Column(name = "completed_at") private Instant completedAt;
    @Column(name = "status") private String status;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "recommendation", indexes = @Index(name = "idx_recommendation_business", columnList = "business_id"))
class Recommendation extends TenantEntity {
    @Column(name = "equipment_id") private UUID equipmentId;
    @Column(name = "source_job_id") private UUID sourceJobId;
    @Column(name = "description", columnDefinition = "text") private String description;
    @Column(name = "priority") private String priority;
    @Column(name = "due_date") private LocalDate dueDate;
    @Column(name = "status") private String status;
    @Column(name = "converted_job_id") private UUID convertedJobId;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "material_catalog_item", indexes = @Index(name = "idx_material_catalog_item_business", columnList = "business_id"))
class MaterialCatalogItem extends TenantEntity {
    @Column(name = "code") private String code;
    @Column(name = "name") private String name;
    @Column(name = "unit_code") private String unitCode;
    @Column(name = "default_price", precision = 19, scale = 4) private BigDecimal defaultPrice;
    @Column(name = "currency", length = 3) private String currency;
    @Column(name = "active") private Boolean active;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "job_material", indexes = @Index(name = "idx_job_material_business", columnList = "business_id"))
class JobMaterial extends TenantEntity {
    @Column(name = "job_id") private UUID jobId;
    @Column(name = "material_catalog_item_id") private UUID materialCatalogItemId;
    @Column(name = "type") private String type;
    @Column(name = "description", columnDefinition = "text") private String description;
    @Column(name = "quantity", precision = 19, scale = 4) private BigDecimal quantity;
    @Column(name = "unit_code") private String unitCode;
    @Column(name = "unit_price", precision = 19, scale = 4) private BigDecimal unitPrice;
    @Column(name = "currency", length = 3) private String currency;
    @Column(name = "source") private String source;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "service_agreement", indexes = @Index(name = "idx_service_agreement_business", columnList = "business_id"))
class ServiceAgreement extends TenantEntity {
    @Column(name = "customer_id") private UUID customerId;
    @Column(name = "number") private String number;
    @Column(name = "type") private String type;
    @Column(name = "status") private String status;
    @Column(name = "valid_from") private LocalDate validFrom;
    @Column(name = "valid_until") private LocalDate validUntil;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "covered_equipment_ids_json", columnDefinition = "jsonb") private String coveredEquipmentIdsJson;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "coverage_rules_json", columnDefinition = "jsonb") private String coverageRulesJson;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "sla_rules_json", columnDefinition = "jsonb") private String slaRulesJson;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "pricing_rules_json", columnDefinition = "jsonb") private String pricingRulesJson;
    @Column(name = "attachment_id") private UUID attachmentId;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "warranty_case", indexes = @Index(name = "idx_warranty_case_business", columnList = "business_id"))
class WarrantyCase extends TenantEntity {
    @Column(name = "equipment_id") private UUID equipmentId;
    @Column(name = "job_id") private UUID jobId;
    @Column(name = "source") private String source;
    @Column(name = "status") private String status;
    @Column(name = "description", columnDefinition = "text") private String description;
    @Column(name = "decision") private String decision;
    @Column(name = "opened_at") private Instant openedAt;
    @Column(name = "resolved_at") private Instant resolvedAt;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "mobile_device", indexes = @Index(name = "idx_mobile_device_business", columnList = "business_id"))
class MobileDevice extends TenantEntity {
    @Column(name = "user_id") private UUID userId;
    @Column(name = "device_identifier") private String deviceIdentifier;
    @Column(name = "platform") private String platform;
    @Column(name = "app_version") private String appVersion;
    @Column(name = "status") private String status;
    @Column(name = "last_seen_at") private Instant lastSeenAt;
    @Column(name = "registered_at") private Instant registeredAt;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "sync_operation", indexes = @Index(name = "idx_sync_operation_business", columnList = "business_id"))
class SyncOperation extends TenantEntity {
    @Column(name = "device_id") private UUID deviceId;
    @Column(name = "client_operation_id") private String clientOperationId;
    @Column(name = "operation_type") private String operationType;
    @Column(name = "subject_type") private String subjectType;
    @Column(name = "subject_id") private UUID subjectId;
    @Column(name = "payload_hash") private String payloadHash;
    @Column(name = "status") private String status;
    @Column(name = "occurred_at") private Instant occurredAt;
    @Column(name = "received_at") private Instant receivedAt;
    @Column(name = "error_code") private String errorCode;
}
