package com.esmpf.maintenance.domain;

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
@Table(name = "maintenance_plan", indexes = @Index(name = "idx_maintenance_plan_business", columnList = "business_id"))
class MaintenancePlan extends TenantEntity {
    @Column(name = "equipment_id", nullable = false) private UUID equipmentId;
    @Column(name = "maintenance_template_id", nullable = false) private UUID maintenanceTemplateId;
    @Column(name = "template_version", nullable = false) private Integer templateVersion;
    @Column(name = "active_from", nullable = false) private LocalDate activeFrom;
    @Column(name = "active_until") private LocalDate activeUntil;
    @Column(name = "next_due_date") private LocalDate nextDueDate;
    @Column(name = "next_due_meter_value", precision = 19, scale = 4) private BigDecimal nextDueMeterValue;
    @Column(name = "last_completed_at") private Instant lastCompletedAt;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "overrides_json", columnDefinition = "jsonb") private String overridesJson;
    @Column(name = "status", nullable = false, length = 40) private String status;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "maintenance_occurrence", indexes = @Index(name = "idx_maintenance_occurrence_business", columnList = "business_id"))
class MaintenanceOccurrence extends TenantEntity {
    @Column(name = "maintenance_plan_id", nullable = false) private UUID maintenancePlanId;
    @Column(name = "due_date") private LocalDate dueDate;
    @Column(name = "due_meter_value", precision = 19, scale = 4) private BigDecimal dueMeterValue;
    @Column(name = "status", nullable = false, length = 40) private String status;
    @Column(name = "service_job_id") private UUID serviceJobId;
    @Column(name = "generated_at", nullable = false) private Instant generatedAt;
    @Column(name = "completed_at") private Instant completedAt;
    @Column(name = "generation_key", nullable = false, length = 200) private String generationKey;
    @Column(name = "reason", length = 500) private String reason;
}
