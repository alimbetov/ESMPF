package com.esmpf.shared.exception;

import java.util.UUID;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String entityName, UUID id) {
        this(entityName, id.toString());
    }

    public EntityNotFoundException(String entityName, String key) {
        super(entityName + " not found: " + key);
    }
}
