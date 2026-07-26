package com.esmpf.catalog.domain;

import com.esmpf.shared.persistence.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "equipment_type", indexes = @Index(name = "idx_equipment_type_business", columnList = "business_id"))
class EquipmentType extends TenantEntity {
    @Column(name = "code") private String code;
    @Column(name = "name") private String name;
    @Column(name = "category") private String category;
    @Column(name = "schema_version") private Integer schemaVersion;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "attribute_schema_json", columnDefinition = "jsonb") private String attributeSchemaJson;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "measurement_schema_json", columnDefinition = "jsonb") private String measurementSchemaJson;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "meter_schema_json", columnDefinition = "jsonb") private String meterSchemaJson;
    @Column(name = "status") private String status;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "job_type", indexes = @Index(name = "idx_job_type_business", columnList = "business_id"))
class JobType extends TenantEntity {
    @Column(name = "code") private String code;
    @Column(name = "name") private String name;
    @Column(name = "category") private String category;
    @Column(name = "default_duration_minutes") private Integer defaultDurationMinutes;
    @Column(name = "default_price", precision = 19, scale = 4) private BigDecimal defaultPrice;
    @Column(name = "requires_checklist") private Boolean requiresChecklist;
    @Column(name = "requires_signature") private Boolean requiresSignature;
    @Column(name = "requires_pdf_report") private Boolean requiresPdfReport;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "settings_json", columnDefinition = "jsonb") private String settingsJson;
    @Column(name = "status") private String status;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "checklist_template", indexes = @Index(name = "idx_checklist_template_business", columnList = "business_id"))
class ChecklistTemplate extends TenantEntity {
    @Column(name = "code") private String code;
    @Column(name = "name") private String name;
    @Column(name = "equipment_type_id") private UUID equipmentTypeId;
    @Column(name = "job_type_id") private UUID jobTypeId;
    @Column(name = "template_version") private Integer templateVersion;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "schema_json", columnDefinition = "jsonb") private String schemaJson;
    @Column(name = "status") private String status;
    @Column(name = "published_at") private Instant publishedAt;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "maintenance_template", indexes = @Index(name = "idx_maintenance_template_business", columnList = "business_id"))
class MaintenanceTemplate extends TenantEntity {
    @Column(name = "code") private String code;
    @Column(name = "name") private String name;
    @Column(name = "equipment_type_id") private UUID equipmentTypeId;
    @Column(name = "job_type_id") private UUID jobTypeId;
    @Column(name = "checklist_template_id") private UUID checklistTemplateId;
    @Column(name = "template_version") private Integer templateVersion;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "schedule_rule_json", columnDefinition = "jsonb") private String scheduleRuleJson;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "reminder_rule_json", columnDefinition = "jsonb") private String reminderRuleJson;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "settings_json", columnDefinition = "jsonb") private String settingsJson;
    @Column(name = "status") private String status;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "unit_of_measure", indexes = @Index(name = "idx_unit_of_measure_business", columnList = "business_id"))
class UnitOfMeasure extends TenantEntity {
    @Column(name = "code") private String code;
    @Column(name = "symbol") private String symbol;
    @Column(name = "name") private String name;
    @Column(name = "quantity_type") private String quantityType;
    @Column(name = "precision_scale") private Integer precisionScale;
    @Column(name = "active") private Boolean active;
}
