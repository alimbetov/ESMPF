package com.esmpf.shared.exception;

import java.util.UUID;

public class StaleEntityException extends RuntimeException {
    public StaleEntityException(String entityName, UUID id, long expectedVersion, long actualVersion) {
        super(entityName + " " + id + " version conflict: expected " + expectedVersion + ", actual " + actualVersion);
    }
}
