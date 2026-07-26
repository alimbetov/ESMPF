package com.esmpf.equipment.domain;

import com.esmpf.shared.persistence.TenantEntity;
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
@Table(name = "equipment", indexes = @Index(name = "idx_equipment_business", columnList = "business_id"))
class Equipment extends TenantEntity {
    @Column(name = "customer_id", nullable = false) private UUID customerId;
    @Column(name = "service_location_id", nullable = false) private UUID serviceLocationId;
    @Column(name = "equipment_type_id", nullable = false) private UUID equipmentTypeId;
    @Column(name = "parent_equipment_id") private UUID parentEquipmentId;
    @Column(name = "name", nullable = false, length = 200) private String name;
    @Column(name = "manufacturer", length = 200) private String manufacturer;
    @Column(name = "model", length = 200) private String model;
    @Column(name = "serial_number", length = 150) private String serialNumber;
    @Column(name = "asset_number", length = 150) private String assetNumber;
    @Column(name = "status", nullable = false, length = 40) private String status;
    @Column(name = "installation_date") private LocalDate installationDate;
    @Column(name = "commissioning_date") private LocalDate commissioningDate;
    @Column(name = "warranty_until") private LocalDate warrantyUntil;
    @Lob @Column(name = "attributes_json") private String attributesJson;
    @Lob @Column(name = "current_meter_values_json") private String currentMeterValuesJson;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "equipment_relation", indexes = @Index(name = "idx_equipment_relation_business", columnList = "business_id"))
class EquipmentRelation extends TenantEntity {
    @Column(name = "source_equipment_id", nullable = false) private UUID sourceEquipmentId;
    @Column(name = "target_equipment_id", nullable = false) private UUID targetEquipmentId;
    @Column(name = "relation_type", nullable = false, length = 80) private String relationType;
    @Column(name = "valid_from") private LocalDate validFrom;
    @Column(name = "valid_until") private LocalDate validUntil;
    @Lob @Column(name = "description") private String description;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "equipment_issue", indexes = @Index(name = "idx_equipment_issue_business", columnList = "business_id"))
class EquipmentIssue extends TenantEntity {
    @Column(name = "equipment_id", nullable = false) private UUID equipmentId;
    @Column(name = "detected_by_job_id") private UUID detectedByJobId;
    @Column(name = "type", nullable = false, length = 80) private String type;
    @Column(name = "severity", nullable = false, length = 40) private String severity;
    @Column(name = "status", nullable = false, length = 40) private String status;
    @Lob @Column(name = "description") private String description;
    @Column(name = "detected_at", nullable = false) private Instant detectedAt;
    @Column(name = "due_date") private LocalDate dueDate;
    @Column(name = "resolved_by_job_id") private UUID resolvedByJobId;
    @Column(name = "resolved_at") private Instant resolvedAt;
}

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "meter_reading", indexes = @Index(name = "idx_meter_reading_business", columnList = "business_id"))
class MeterReading extends TenantEntity {
    @Column(name = "equipment_id", nullable = false) private UUID equipmentId;
    @Column(name = "meter_code", nullable = false, length = 100) private String meterCode;
    @Column(name = "reading_value", nullable = false, precision = 19, scale = 4) private BigDecimal readingValue;
    @Column(name = "unit_code", nullable = false, length = 40) private String unitCode;
    @Column(name = "recorded_at", nullable = false) private Instant recordedAt;
    @Column(name = "recorded_by") private UUID recordedBy;
    @Column(name = "source", nullable = false, length = 40) private String source;
}
