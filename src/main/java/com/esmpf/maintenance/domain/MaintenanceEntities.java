package com.esmpf.maintenance.domain;

import com.esmpf.shared.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "maintenance_plan", indexes = @Index(name = "idx_maintenance_plan_business", columnList = "business_id"))
class MaintenancePlan extends BaseEntity {
    @Column(name = "equipment_id") private UUID equipmentId;
    @Column(name = "maintenance_template_id") private UUID maintenanceTemplateId;
    @Column(name = "template_version") private Integer templateVersion;
    @Column(name = "active_from") private LocalDate activeFrom;
    @Column(name = "active_until") private LocalDate activeUntil;
    @Column(name = "next_due_date") private LocalDate nextDueDate;
    @Column(name = "next_due_meter_value") private BigDecimal nextDueMeterValue;
    @Column(name = "last_completed_at") private Instant lastCompletedAt;
    @Lob @Column(name = "overrides_json") private String overridesJson;
    @Column(name = "status") private String status;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "maintenance_occurrence", indexes = @Index(name = "idx_maintenance_occurrence_business", columnList = "business_id"))
class MaintenanceOccurrence extends BaseEntity {
    @Column(name = "maintenance_plan_id") private UUID maintenancePlanId;
    @Column(name = "due_date") private LocalDate dueDate;
    @Column(name = "due_meter_value") private BigDecimal dueMeterValue;
    @Column(name = "status") private String status;
    @Column(name = "service_job_id") private UUID serviceJobId;
    @Column(name = "generated_at") private Instant generatedAt;
    @Column(name = "completed_at") private Instant completedAt;
    @Column(name = "generation_key") private String generationKey;
    @Column(name = "reason") private String reason;
}
