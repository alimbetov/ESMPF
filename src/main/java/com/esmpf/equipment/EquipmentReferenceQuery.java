package com.esmpf.equipment;

import static com.esmpf.equipment.EquipmentDtos.EquipmentReference;

import java.util.UUID;

public interface EquipmentReferenceQuery {
    EquipmentReference requireEquipment(UUID equipmentId);
}
