package com.esmpf.catalog;

import static com.esmpf.catalog.CatalogDtos.*;

import java.util.UUID;

public interface CatalogReferenceQuery {
    EquipmentTypeReference requireEquipmentType(UUID id);
    JobTypeReference requireJobType(UUID id);
    ChecklistTemplateReference requireChecklistTemplate(UUID id);
    MaintenanceTemplateReference requireMaintenanceTemplate(UUID id);
}
