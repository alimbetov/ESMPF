package com.esmpf.shared.security;

public class SecurityAccessException extends RuntimeException {

    public SecurityAccessException(String message) {
        super(message);
    }

    public SecurityAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
